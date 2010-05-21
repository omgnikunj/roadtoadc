package org.mailboxer.saymyname.listeners;

import org.mailboxer.saymyname.services.ManagerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

public class K9MailListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, final Intent intent) {
		if (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() != TelephonyManager.CALL_STATE_IDLE) {
			return;
		}

		Log.v("SMN", "mail");

		try {
			context = context.createPackageContext("org.mailboxer.saymyname", 0);
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		}

		final Intent serviceIntent = new Intent(context, ManagerService.class);
		serviceIntent.putExtra("org.mailboxer.saymyname.number", intent.getStringExtra("com.fsck.k9.intent.extra.FROM"));
		serviceIntent.putExtra("org.mailboxer.saymyname.message", intent.getStringExtra("com.fsck.k9.intent.extra.SUBJECT"));
		serviceIntent.putExtra("org.mailboxer.saymyname.mail", "mail");
		context.startService(serviceIntent);
	}
}