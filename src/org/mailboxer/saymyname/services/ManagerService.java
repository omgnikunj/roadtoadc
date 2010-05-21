package org.mailboxer.saymyname.services;

import org.mailboxer.saymyname.threads.CallThread;
import org.mailboxer.saymyname.threads.MailThread;
import org.mailboxer.saymyname.threads.SmsThread;
import org.mailboxer.saymyname.utils.Contact;
import org.mailboxer.saymyname.utils.Settings;
import org.mailboxer.saymyname.utils.Speaker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ManagerService extends Service {
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

		speaker = new Speaker(this);
	}

	@Override
	public void onDestroy() {
		Log.v("SMN", "onDestroy");

		if (speaker != null) {
			speaker.shutdown();
		}
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		if (!settings.isStartSomething()) {
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