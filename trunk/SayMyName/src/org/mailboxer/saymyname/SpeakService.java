package org.mailboxer.saymyname;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Contacts.People;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.tts.TTS;

public class SpeakService extends Service {
	private String caller;

	private boolean repeat;
	private int repeatSeconds;

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
					startSpeaking(getCallerID(incomingNumber));
				}

				super.onCallStateChanged(state, incomingNumber);
			}
		};

		telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		talker = new TTS(this, ttsInitListener, true);

		super.onCreate();
	}


	private void readPreferences() {
		if(preferences.getString("repeatSeconds", null) != null) {
			repeatSeconds = Integer.parseInt(preferences.getString("repeatSeconds", null));

			if(repeatSeconds > 0) {
				repeat = true;
			} else {
				repeat = false;
			}
		}
	}


	// from:
	// http://www.kluit.com/blog/index.php?/archives/10-Android-Example-A-contact-list-+-using-the-Phone.html
	// http://androidcommunity.com/forums/f3/cant-retrieve-the-data-from-contacts-7230/
	private String getCallerID(String incomingNumber) {
		String[] projection = new String[] {
				People.NAME,
				People.NUMBER,
		};

		Cursor c = getContentResolver().query(People.CONTENT_URI, projection, null, null, null);

		String result = null;

		if (c.moveToFirst()) {
			do {
				if(c.getString(1) != null) {
					if(c.getString(1).equals(incomingNumber)) {
						result = c.getString(0);
					}
				}
			} while (c.moveToNext());
		}

		return result;
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

		// turn volume up (to maximum - 2)
		for(int i = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC); i > speakVolume++; i--) {
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
		}
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