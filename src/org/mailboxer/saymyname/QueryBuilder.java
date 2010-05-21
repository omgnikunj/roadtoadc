package org.mailboxer.saymyname;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;

public class QueryBuilder extends Service {
	public static final int COMMUNICATION_CALL = 1;
	public static final int COMMUNICATION_SMS = 2;
	public static final int COMMUNICATION_EMAIL = 3;
	public static final String SHUTDOWN_CMD = "shutdown";
	public static final String STOP_CMD = "stop";
	public static final String MESSAGE = "message";
	public static final String NUMBER = "number";
	public static final String EMAIL = "email";
	public static final String SUBJECT = "subject";
	public static final String COMMUNICATION_TYPE = "communication_type";

	private Speaker talker;
	private String text;
	private ContactSwitcher callerInfo;
	private Settings settings;
	private int communicationType;
	private String msg;
	private LoopThread thread;
	private boolean started;
	private boolean shutdown;
	private boolean smsRunning;
	private int smsReadCounter;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (intent.getBooleanExtra(SHUTDOWN_CMD, false)) {
			// one of the listeners requested to shutdown the service
			shutdown();
			return;
		}
		if (intent.getBooleanExtra(STOP_CMD, false)) {
			// one of the listeners requested to stop the service
			// (don't shutdown! stopping prevents SayMyName to speak during a running call!)

			// prevent the looper to go on 
			shutdown = true;

			if (talker != null) {
				talker.shutdown();
				talker = null;
			}
			if (thread != null) {
				thread.interrupt();
				thread = null;
			}
			return;
		}
		if (started) {
			if(smsRunning) {
				// sms is running, but a call is more important - interrupt sms-speech and start call-speech
				talker.shutdown();
			} else {
				// SayMyName is running (or stopped) and we don't want it to start a new speech
				// (because there is a running call or another announced call)
				return;
			}
		}

		settings = new Settings(this);

		if (!settings.isStartSomething()) {
			// the user disabled SayCaller, SaySMS and SayEMail - shutdown and do nothing
			shutdown();
			return;
		}

		if (settings.isDiscreetMode()) {
			if (!isDiscreet()) {
				// user wants only discreet announcements and we are not discreet - shutdown and do nothing
				shutdown();
				return;
			}
		}

		thread = new LoopThread();
		talker = new Speaker(this, this);

		// sms or call?
		communicationType = intent.getIntExtra(QueryBuilder.COMMUNICATION_TYPE, 0);

		switch (communicationType) {
		case COMMUNICATION_CALL:
			if (settings.isStartSayCaller()) {
				callerInfo = ContactSwitcher.getInstance(this, intent.getStringExtra(QueryBuilder.NUMBER), settings);

				smsRunning = false;
				text = callerInfo.buildString(settings.getCallerFormatString());
			} else {
				shutdown();
			}
			break;

		case COMMUNICATION_SMS:
			smsReadCounter = 0;

			if (settings.isStartSaySMS()) {
				callerInfo = ContactSwitcher.getInstance(this, intent.getStringExtra(QueryBuilder.NUMBER), settings);

				smsRunning = true;
				msg = intent.getStringExtra(QueryBuilder.MESSAGE);
				text = callerInfo.buildString(settings.getSmsFormatString());
			} else {
				shutdown();
			}
			break;

		case COMMUNICATION_EMAIL:
			// Disguise as sms. No need for another counter.
			smsReadCounter = 0;

			if (settings.isStartSayEMail()) {

				thread.initializeForMessageLoop( 
						settings.isEMailReadSubject(), 
						settings.getEMailReadSubjectDelay(), 
						settings.isEMailReadSubjectDiscreet(), 
						settings.getEMailRepeatTimesSubject() );

				callerInfo = ContactSwitcher.getInstance(
						this, 
						intent.getStringExtra(QueryBuilder.EMAIL), 
						"<not used, only to call different constructor>", 
						settings);

				smsRunning = true;

				text = callerInfo.buildString(settings.getEMailFormatString());
				msg = prepareSubject( intent.getStringExtra(QueryBuilder.SUBJECT) );

			} else {
				shutdown();
			}
			break;
		}

		super.onStart(intent, startId);
	}

	private boolean isDiscreet() {
		if (((AudioManager) getSystemService(AUDIO_SERVICE)).getRouting(AudioManager.MODE_RINGTONE) != AudioManager.ROUTE_SPEAKER) {
			// audio-routing of ringtone is different than phone's speakers - we are discreet
			return true;
		}
		return false;
	}

	public void looper() {
		if (!shutdown) {
			// nobody requested a shutdown - go on
			if (started) {
				// thread is already started - go on
				thread.run();
			} else {
				// seems to be the first run - start the thread
				thread.start();
			}
		}
	}

	private String prepareSubject( String subject ){

		String result;
		if ( subject == null ){
			result = "";
		}
		result = subject;	

		if ( settings.isEMailCutReFwd()){
			if ( result.startsWith("Re:")){
				result = result.replaceFirst("Re:", "");
			}
			if ( result.startsWith("Fwd:")){
				result = result.replaceFirst("Fwd:", "");
			}
		}

		return result;
	}

	private void prepareMessage() {
		String[] splitted = msg.split(" ");

		// split... split... split...
		for (int i = 0; i < splitted.length; i += 3) {
			String part = "";
			if (i + 3 < splitted.length) {
				for (int j = 0; j < 3; j++) {
					part += splitted[i + j];
				}
			} else {
				for (int j = 0; j < splitted.length - i; j++) {
					part += splitted[i + j];
				}
			}
			// say the actual message part
			// (this doesn't invoke OnUtteranceCompletedListener in Speaker.java !)
			talker.messageSpeak(part);
		}

		// invokes OnUtteranceCompletedListener
		talker.speak(" ");
	}

	private void shutdown() {
		shutdown = true;

		if(talker != null) {
			talker.shutdown();
			talker = null;
		}
		if(thread != null) {
			thread.interrupt();
			thread = null;
		}

		started = false;
		stopSelf();
	}

	@Override
	public void onDestroy() {
		shutdown();
		super.onDestroy();
	}

	private class LoopThread extends Thread {
		/*
		 * Settings for smsLoop
		 */
		boolean isMessageRead = false;
		int messageReadDelay = 1000;
		boolean isMessageReadDiscreet = true;
		int messageRepeatTimes = 1;

		// saves actual loop-step
		private int loopCounter = 1;

		public void initializeForMessageLoop(boolean isMessageRead, int messageReadDelay, boolean isMessageReadDiscreet, int messageRepeatTimes){
			this.isMessageRead = isMessageRead;
			this.messageReadDelay = messageReadDelay;
			this.isMessageReadDiscreet = isMessageReadDiscreet;
			this.messageRepeatTimes = messageRepeatTimes;
		}

		@Override
		public synchronized void start() {
			started = true;
			super.start();
		}

		@Override
		public void run() {
			// sms or call?
			switch (communicationType) {
			case COMMUNICATION_CALL:
				callLoop();
				break;

			case COMMUNICATION_SMS:
				smsLoop();
				break;

			case COMMUNICATION_EMAIL:
				messageLoop();
				break;
			}

			super.run();
		}

		private void callLoop() {
			if(loopCounter == 1) {
				// speak name
				talker.speak(text);

				loopCounter++;
				return;
			}

			if (loopCounter > settings.getCallerRepeatTimes()) {
				// repeated the name often enough
				return;
			}

			try {
				sleep(settings.getCallerRepeatSeconds() * 1000);
			} catch (InterruptedException e) {}

			talker.speak(text);

			loopCounter++;
		}

		/**
		 * Use messageLoop instead, 
		 * don't forget to call initialiseForMessageLoop first when you use 
		 * messageLoop
		 */
		@Deprecated
		private void smsLoop() {
			switch (loopCounter) {
			case 1:
				// speak name
				talker.speak(text);

				loopCounter++;

				break;

			case 2:
				smsReadCounter++;
				if (settings.isSmsRead()) {
					// user wants to hear the whole sms
					try {
						sleep(settings.getSmsReadDelay() * 1000);
					} catch (InterruptedException e) {}

					if (settings.isSmsReadDiscreet()) {
						if (isDiscreet()) {
							prepareMessage();
						}
					} else {
						// no matter if discreet or not - read sms
						prepareMessage();
					}
				} else {
					talker.speak("");
				}

				if (smsReadCounter == settings.getSmsRepeatTimes()) {
					loopCounter++;
				}
				break;

			case 3:
				try {
					sleep(settings.getSmsReadDelay() * 1000);
				} catch (InterruptedException e) {}

				// speak name
				talker.speak(text);

				loopCounter++;

				shutdown();
				break;
			}
		}

		private void messageLoop() {
			switch (loopCounter) {
			case 1:
				// speak name
				talker.speak(text);

				loopCounter++;

				break;

			case 2:
				smsReadCounter++;
				if (this.isMessageRead) {
					// user wants to hear the whole sms
					try {
						sleep(this.messageReadDelay * 1000);
					} catch (InterruptedException e) {}

					if (this.isMessageReadDiscreet) {
						if (isDiscreet()) {
							prepareMessage();
						}
					} else {
						// no matter if discreet or not - read sms
						prepareMessage();
					}
				} else {
					talker.speak("");
				}

				if (smsReadCounter == this.messageRepeatTimes) {
					loopCounter++;
				}
				break;

			case 3:
				try {
					sleep(this.messageReadDelay * 1000);
				} catch (InterruptedException e) {}

				// speak name
				talker.speak(text);

				loopCounter++;

				shutdown();
				break;
			}
		}
	}
}