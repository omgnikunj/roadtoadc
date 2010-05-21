package org.mailboxer.android.threads;

import org.mailboxer.android.services.ManagerService;
import org.mailboxer.android.utils.Contact;
import org.mailboxer.android.utils.Settings;
import org.mailboxer.android.utils.Speaker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MailThread extends Thread {
	private final Context context;
	private final Settings settings;
	private final Speaker speaker;
	private String subject;
	private String text;

	public MailThread(final Context context, final Speaker speaker, final Settings settings, final String sender, final String message) {
		this.context = context;
		this.settings = settings;
		this.speaker = speaker;
		text = sender;

		if (!settings.isStartSayEMail()) {
			return;
		}

		if (message != null) {
			subject = message;
			subject = subject.replace("Re:", "");
			subject = subject.replace("Fwd:", "");
		} else {
			subject = "";
		}

		if (!text.equals(Contact.UNKNOWN) && !text.equals("")) {
			text = settings.getMailFormat().replaceFirst("%", text);
		}

		start();
	}

	@Override
	public void run() {
		Log.v("SMN", "speak");
		speaker.speak(text);

		if (settings.isEMailReadSubject()) {
			try {
				sleep(settings.getEMailReadDelay());
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

			speaker.speak(subject);

			try {
				sleep(settings.getEMailReadDelay());
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

			speaker.speak(text);
		}

		context.stopService(new Intent(context, ManagerService.class));
	}
}