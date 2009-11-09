package org.mailboxer.saymyname;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

public class QueryBuilder extends Service {
	public static final int COMMUNICATION_CALL = 1;
	public static final int COMMUNICATION_SMS = 2;
	public static final String SHUTDOWN_CMD = "shutdown";
	public static final String STOP_CMD = "stop";
	public static final String MESSAGE = "message";
	public static final String NUMBER = "number";
	public static final String COMMUNICATION_TYPE = "communication_type";

	private Speaker talker;
	private String text;
	private Caller callerInfo;
	private Settings settings;
	private int communicationType;
	private String msg;
	private LoopThread thread;
	private boolean started;
	private boolean shutdown;
	private boolean smsRunning;

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
		if(intent.getBooleanExtra(SHUTDOWN_CMD, false)) {
			Log.e("SNM", "SHUTDOWN_CMD");
			shutdown();
			return;
		}
		if(intent.getBooleanExtra(STOP_CMD, false)) {
			Log.e("SNM", "STOP_CMD");
			if(talker != null) {
				talker.shutdown();
			}
			return;
		}
		if(started) {
			if(smsRunning) {
				talker.shutdown();
			} else {
				Log.e("SNM", "RETURNED");
				return;
			}
		}

		settings = new Settings(this);

		if (!settings.isStartSomething()) {
			shutdown();
			return;
		}

		if (settings.isDiscreetMode()) {
			if (!isDiscreet()) {
				shutdown();
				return;
			}
		}

		Log.e("SNM", "CALLER CREATE");
		callerInfo = new Caller(this, intent.getStringExtra(QueryBuilder.NUMBER), settings);

		thread = new LoopThread();
		talker = new Speaker(this, this);

		communicationType = intent.getIntExtra(QueryBuilder.COMMUNICATION_TYPE, 0);

		switch (communicationType) {
		case COMMUNICATION_CALL:
			if (settings.isStartSayCaller()) {
				smsRunning = false;
				text = callerInfo.buildString(settings.getCallerFormatString());
			} else {
				shutdown();
			}
			break;

		case COMMUNICATION_SMS:
			if (settings.isStartSaySMS()) {
				smsRunning = true;
				msg = intent.getStringExtra(QueryBuilder.MESSAGE);
				text = callerInfo.buildString(settings.getSmsFormatString());
			} else {
				shutdown();
			}
			break;
		}

		super.onStart(intent, startId);
	}

	private boolean isDiscreet() {
		if (((AudioManager) getSystemService(AUDIO_SERVICE)).getRouting(AudioManager.MODE_RINGTONE) != AudioManager.ROUTE_SPEAKER) {
			return true;
		}
		return false;
	}

	public void looper() {
		if (!shutdown) {
			if (started) {
				thread.run();
			} else {
				thread.start();
			}
		}
	}

	private void prepareMessage() {
		String[] splitted = msg.split(" ");

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
			talker.messageSpeak(part);
		}

		talker.speak(" ");
	}

	private void shutdown() {
		Log.e("SAYMYNAME", "SHUTDOWN");

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
		private int loopCounter = 1;

		@Override
		public synchronized void start() {
			started = true;
			super.start();
		}

		@Override
		public void run() {
			switch (communicationType) {
			case COMMUNICATION_CALL:
				callLoop();
				break;

			case COMMUNICATION_SMS:
				smsLoop();
				break;
			}

			super.run();
		}

		private void callLoop() {
			if(loopCounter == 1) {
				talker.speak(text);

				loopCounter++;
				return;
			}

			if (loopCounter > settings.getCallerRepeatTimes()) {
				return;
			}

			try {
				sleep(settings.getCallerRepeatSeconds() * 1000);
			} catch (InterruptedException e) {}

			talker.speak(text);

			loopCounter++;
		}



		private void smsLoop() {
			switch (loopCounter) {
			case 1:
				talker.speak(text);

				loopCounter++;

				break;

			case 2:
				if (settings.isSmsRead()) {
					try {
						sleep(settings.getSmsReadDelay() * 1000);
					} catch (InterruptedException e) {}

					if (settings.isSmsReadDiscreet()) {
						if (isDiscreet()) {
							prepareMessage();
						}
					} else {
						prepareMessage();
					}
				} else {
					talker.speak("");
				}

				loopCounter++;
				break;

			case 3:
				try {
					sleep(settings.getSmsReadDelay() * 1000);
				} catch (InterruptedException e) {}

				talker.speak(text);

				loopCounter++;

				shutdown();
				break;
			}
		}
	}
}