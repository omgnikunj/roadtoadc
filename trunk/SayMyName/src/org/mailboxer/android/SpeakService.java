package org.mailboxer.android;

import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.tts.TTS;

public class SpeakService extends Service {
	private boolean start;
	private boolean repeat;
	private int repeatSeconds;
	private int repeatTimes;
	private boolean cut;
	private boolean speakSilent;
	private int wantedVolume;

	private boolean onPhone;

	private String caller;
	private String test;

	private TTSListener ttsInitListener = new TTSListener();
	private TTS talker;

	private TelephonyManager telephonyManager;
	private AudioManager audio;
	private SharedPreferences preferences;

	private SpeakThread thread;


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// Test-Speak?
		Bundle extras = intent.getExtras();

		if(extras != null) {
			if(extras.getString("say") != null) {
				test = extras.getString("say"); 
			}
		}


		readPreferences();
		if(!start && thread != null) {
			shutdown();

			return;
		}


		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		preferences = getSharedPreferences("saymyname", MODE_WORLD_WRITEABLE);
		readPreferences();

		if(!start) {
			stopSelf();

			return;
		}

		thread = new SpeakThread();
		thread.start();

		talker = new TTS(SpeakService.this, ttsInitListener, false);
		talker.setLanguage(Locale.getDefault().getLanguage());


		super.onCreate();
	}

	private void readPreferences() {
		start = preferences.getBoolean("start", false);

		speakSilent = !preferences.getBoolean("silent", true);

		wantedVolume = Integer.parseInt(preferences.getString("volume", "12"));

		if(preferences.getString("repeatSeconds", null) != null) {
			repeatSeconds = Integer.parseInt(preferences.getString("repeatSeconds", null));

			if(repeatSeconds > 0) {
				repeat = true;
			} else {
				repeat = false;

				if(repeatSeconds < 0) {
					repeatSeconds = 0;
				}
			}
		}

		if(preferences.getString("repeatTimes", null) != null) {
			repeatTimes = Integer.parseInt(preferences.getString("repeatTimes", null));

			if(repeatTimes > 0) {
				repeat = true;
			} else {
				repeat = false;

				if(repeatTimes < 0) {
					repeatTimes = 0;
				}
			}
		}

		cut = preferences.getBoolean("cut", false);
	}


	private String getCallerID(String incomingNumber) {
		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(incomingNumber));
		Cursor cur = getContentResolver().query(contactUri, new String[] {People.NAME, People.NUMBER}, null, null, null);

		if (cur.moveToFirst()) {
			do {
				if(cut && cur.getString(0) != null) {
					return cur.getString(0).split(" ")[0];
				} else {
					return cur.getString(0);
				}
			} while (cur.moveToNext());
		}

		return null;
	}


	private void doSpeak(String text) {
		if(talker != null) {
			talker.speak(text, 0, null);
		}
	}


	private void adjustSpeakVolume() {
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int speakVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);

		// silent?
		if(currentVolume == 0 && speakSilent == false) {
			for(int i = speakVolume; i > currentVolume; i--) {
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
			}
		} else {
			if(wantedVolume > speakVolume) {
				for(int i = speakVolume; i < wantedVolume; i++) {
					audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
				}
			} else {
				for(int i = speakVolume; i > wantedVolume; i--) {
					audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
				}				
			}
		}
	}

	private void shutdown() {
		if(thread != null) {
			thread.stop();

			thread = null;
		}

		if(talker != null) {
			talker.shutdown();

			talker = null;
		}

		telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);

		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("start", false);
		editor.commit();


		stopSelf();
	}

	@Override
	public void onDestroy() {
		if(thread != null) {
			shutdown();
		}

		super.onDestroy();
	}


	private class SpeakThread extends Thread {
		@Override
		public void run() {
			// listen for phoneState change
			telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
		}

		private void speakLoop() {
			// start speaking
			int loops = 1;

			do {
				loops++;
				doSpeak(caller);

				try {
					sleep(repeatSeconds * 1000);
				} catch (InterruptedException e) {}
			} while(repeat && loops <= repeatTimes && telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING);
		}
	}

	PhoneStateListener phoneListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if(state == TelephonyManager.CALL_STATE_RINGING && onPhone == false) {
				adjustSpeakVolume();

				String callerID = getCallerID(incomingNumber);
				if(callerID != null) {
					caller = callerID;
					thread.speakLoop();
				} else {
					caller = getResources().getString(R.string.unknown_caller);
					thread.speakLoop();
				}
			} else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
				onPhone = true;
			} else {
				thread.stop();
				caller = null;
				onPhone = false;
			}

			super.onCallStateChanged(state, incomingNumber);
		}
	};

	private class TTSListener implements TTS.InitListener {
		public void onInit(int arg0) {
			if(test != null) {
				adjustSpeakVolume();

				doSpeak(test);
				test = null;
			}
		}
	}
}