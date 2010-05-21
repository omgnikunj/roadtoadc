package org.mailboxer.saymyname.threads;

import org.mailboxer.saymyname.services.ManagerService;
import org.mailboxer.saymyname.utils.Contact;
import org.mailboxer.saymyname.utils.Settings;
import org.mailboxer.saymyname.utils.Speaker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SmsThread extends Thread {
	private final Context context;
	private final String message;
	private final Settings settings;
	private final Speaker speaker;
	private String text;

	public SmsThread(final Context context, final Speaker speaker, final Settings settings, final String text, final String message) {
		this.context = context;
		this.settings = settings;
		this.speaker = speaker;
		this.message = message;
		this.text = text;

		if (!settings.isStartSaySMS()) {
			return;
		}

		if (!text.equals(Contact.UNKNOWN) && !text.equals("")) {
			this.text = settings.getSmsFormat().replaceFirst("%", text);
		}

		start();
	}

	@Override
	public void run() {
		Log.v("SMN", "speak");
		speaker.speak(text);

		try {
			sleep(settings.getSmsReadDelay());
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		if (settings.isSmsRead()) {
			speaker.speak(message);

			try {
				sleep(settings.getSmsReadDelay());
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

			speaker.speak(text);
		}

		context.stopService(new Intent(context, ManagerService.class));
	}
}