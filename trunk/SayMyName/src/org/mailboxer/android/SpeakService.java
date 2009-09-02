package org.mailboxer.android;

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
	private String caller;

	private boolean repeat;
	private int repeatSeconds;

	private boolean speakSilent;
	private int wantedVolume;

	private TTS.InitListener ttsInitListener = new TTS.InitListener() {

		public void onInit(int arg0) {}
	};
	private TTS talker;

	private TelephonyManager telephonyManager;
	private AudioManager audio;
	private SharedPreferences preferences;

	private SpeakThread thread = new SpeakThread();


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// read preferences
		readPreferences();


		// Test-Speak?
		Bundle extras = intent.getExtras();

		if(extras != null) {
			adjustSpeakVolume();
			doSpeak(extras.getString("say"));
		}

		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		preferences = getSharedPreferences("saymyname", MODE_WORLD_WRITEABLE);

		// listen for phoneState change
		PhoneStateListener phoneListener = new PhoneStateListener() {

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				readPreferences();

				if(state == TelephonyManager.CALL_STATE_RINGING) {
					String callerID = getCallerID(incomingNumber);
					if(callerID != null) {
						startSpeaking(callerID);
					} else {
						startSpeaking("Unknown");
					}
				}

				super.onCallStateChanged(state, incomingNumber);
			}
		};

		telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		talker = new TTS(this, ttsInitListener, false);

		super.onCreate();
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
		String[] projection = new String[] {
				People.NAME,
				People.NUMBER,
		};

		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(incomingNumber));
		Cursor cur = getContentResolver().query(contactUri, projection, null, null, null);

		if (cur.moveToFirst()) {
			do {
				return cur.getString(0);
			} while (cur.moveToNext());
		}

		return null;
	}


	private void startSpeaking(String caller) {
		this.caller = caller;

		adjustSpeakVolume();

		// should i repeat the caller?
		if(repeat) {
			// yes
			thread.run();
		} else {
			// no - speak only once
			thread.run();
		}
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
		talker.shutdown();
		thread.stop();

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