package org.mailboxer.saymyname.listeners;

import org.mailboxer.saymyname.services.ManagerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SmsListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, final Intent intent) {
		if (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() != TelephonyManager.CALL_STATE_IDLE) {
			return;
		}

		Log.v("SMN", "sms");

		final Bundle bundle = intent.getExtras();
		final Object[] pdusObj = (Object[]) bundle.get("pdus");

		final SmsMessage[] messages = new SmsMessage[pdusObj.length];
		for (int i = 0; i < pdusObj.length; i++) {
			messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
		}

		final StringBuilder sb = new StringBuilder();

		if (messages.length > 1) {
			for (final SmsMessage currentMessage : messages) {
				sb.append(currentMessage.getDisplayMessageBody());
				sb.append('\n');
			}
		} else {
			sb.append(messages[0].getDisplayMessageBody());
		}

		try {
			context = context.createPackageContext("org.mailboxer.saymyname", 0);
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		}

		final Intent serviceIntent = new Intent(context, ManagerService.class);
		serviceIntent.putExtra("org.mailboxer.saymyname.number", messages[0].getDisplayOriginatingAddress());
		serviceIntent.putExtra("org.mailboxer.saymyname.message", sb.toString());
		context.startService(serviceIntent);
	}
}