package org.mailboxer.saymyname.threads;

import org.mailboxer.saymyname.services.ManagerService;
import org.mailboxer.saymyname.utils.Contact;
import org.mailboxer.saymyname.utils.Settings;
import org.mailboxer.saymyname.utils.Speaker;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallThread extends Thread {
	private final Context context;
	private final Settings settings;
	private final Speaker speaker;
	private String text;

	public CallThread(final Context context, final Speaker speaker, final Settings settings, final String text) {
		this.context = context;
		this.speaker = speaker;
		this.settings = settings;
		this.text = text;

		if (!settings.isStartSayCaller()) {
			return;
		}

		if (!text.equals(Contact.UNKNOWN) && !text.equals("")) {
			this.text = settings.getCallerFormat().replaceFirst("%", text);
		}

		start();
	}

	@Override
	public void run() {
		int counter = 0;
		while (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() == TelephonyManager.CALL_STATE_RINGING && counter < settings.getCallerRepeatTimes()) {
			Log.v("SMN", "speak");
			speaker.speak(text);

			try {
				sleep(settings.getCallerRepeatSeconds());
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

			counter++;
		}

		context.stopService(new Intent(context, ManagerService.class));
	}
}