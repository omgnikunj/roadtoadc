package org.mailboxer.android.services;

import org.mailboxer.android.threads.CallThread;
import org.mailboxer.android.threads.MailThread;
import org.mailboxer.android.threads.SmsThread;
import org.mailboxer.android.utils.Contact;
import org.mailboxer.android.utils.Settings;
import org.mailboxer.android.utils.Speaker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

public class ManagerService extends Service {
	private AudioManager audio;
	private int previousVolume;
	private Settings settings;
	private Speaker speaker;

	@Override
	public IBinder onBind(final Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.v("SMN", "onCreate");
		settings = new Settings(this);

		if (!settings.isStartSomething()) {
			stopSelf();
			return;
		}

		if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			stopSelf();
			return;
		}

		speaker = new Speaker(this);
	}

	@Override
	public void onDestroy() {
		Log.v("SMN", "onDestroy");

		if (speaker != null) {
			speaker.shutdown();

			audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
		}
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		if (!settings.isStartSomething()) {
			stopSelf();
			return;
		}

		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		if (audio.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			stopSelf();
			return;
		}

		if (intent.getStringExtra("org.mailboxer.saymyname.number") == null) {
			stopSelf();
			return;
		}

		final String incomingNumber = intent.getStringExtra("org.mailboxer.saymyname.number");
		final String message = intent.getStringExtra("org.mailboxer.saymyname.message");
		final String mail = intent.getStringExtra("org.mailboxer.saymyname.mail");

		final String text = Contact.getCaller(incomingNumber, this, settings);

		if (text == null) {
			stopSelf();
			return;
		}

		previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamVolume(AudioManager.STREAM_RING) * 2, 0);

		if (message == null) {
			new CallThread(this, speaker, settings, text);
		} else {
			if (mail == null) {
				new SmsThread(this, speaker, settings, text, message);
			} else {
				new MailThread(this, speaker, settings, text, message);
			}
		}
	}
}