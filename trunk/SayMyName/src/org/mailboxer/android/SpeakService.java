package org.mailboxer.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
	private String caller;

	private boolean repeat;
	private int repeatSeconds;

	private boolean speakSilent;
	private int wantedVolume;

	private TTS.InitListener ttsInitListener = new TTS.InitListener() {
		public void onInit(int arg0) {}
	};
	private TTS talker;

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {}

		public void onServiceDisconnected(ComponentName name) {
			startTTS();
		}
	};

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
		readPreferences();


		// Test-Speak?
		Bundle extras = intent.getExtras();

		if(extras != null) {
			if(extras.getString("say") != null) {
				adjustSpeakVolume();

				doSpeak(extras.getString("say"));
			}
		}


		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		preferences = getSharedPreferences("saymyname", MODE_WORLD_WRITEABLE);

		readPreferences();


		// listen for phoneState change
		PhoneStateListener phoneListener = new PhoneStateListener() {

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if(state == TelephonyManager.CALL_STATE_RINGING) {
					adjustSpeakVolume();

					String callerID = getCallerID(incomingNumber);
					if(callerID != null) {
						caller = callerID;
						thread.run();
					} else {
						caller = "Unknown";
						thread.run();
					}
				} else {
					thread.stop();
					caller = null;
				}

				super.onCallStateChanged(state, incomingNumber);
			}
		};

		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


		thread = new SpeakThread();

		startTTS();


		super.onCreate();
	}

	private void startTTS() {
		talker = new TTS(SpeakService.this, ttsInitListener, false);

		Intent serviceIntent = new Intent("android.intent.action.USE_TTS");
		serviceIntent.addCategory("android.intent.category.TTS");

		bindService(serviceIntent, connection, BIND_AUTO_CREATE);
	}

	private void readPreferences() {
		// very confusing :P
		if(preferences.getBoolean("silent", false)) {
			speakSilent = false;
		} else {
			speakSilent = true;
		}

		wantedVolume = Integer.parseInt(preferences.getString("volume", "12"));

		if(preferences.getString("repeatSeconds", null) != null) {
			repeatSeconds = Integer.parseInt(preferences.getString("repeatSeconds", null));

			if(repeatSeconds > 0) {
				repeat = true;
			} else {
				repeat = false;
			}
		}
	}


	private String getCallerID(String incomingNumber) {
		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(incomingNumber));
		Cursor cur = getContentResolver().query(contactUri, new String[] {People.NAME, People.NUMBER}, null, null, null);

		if (cur.moveToFirst()) {
			do {
				return cur.getString(0);
			} while (cur.moveToNext());
		}

		return null;
	}


	private void doSpeak(String text) {
		talker.speak(text, 0, null);
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

	@Override
	public void onDestroy() {
		thread.stop();
		unbindService(connection);

		super.onDestroy();
	}

	private class SpeakThread extends Thread {
		@Override
		public void run() {
			// start speaking
			do {
				doSpeak(caller);

				try {
					sleep(repeatSeconds * 1000);
				} catch (InterruptedException e) {}
			} while(telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING && repeat);
		}
	}
}