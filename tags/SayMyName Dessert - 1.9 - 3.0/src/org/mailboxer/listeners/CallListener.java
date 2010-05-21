package org.mailboxer.listeners;

import org.mailboxer.saymyname.QueryBuilder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {

			Intent serviceIntent = new Intent(context, QueryBuilder.class);
			serviceIntent.putExtra(QueryBuilder.COMMUNICATION_TYPE, QueryBuilder.COMMUNICATION_CALL);
			serviceIntent.putExtra(QueryBuilder.NUMBER, intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
			context.startService(serviceIntent);

		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

			Intent serviceIntent = new Intent(context, QueryBuilder.class);
			serviceIntent.putExtra(QueryBuilder.STOP_CMD, true);
			context.startService(serviceIntent);

		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {

			Intent serviceIntent = new Intent(context, QueryBuilder.class);
			serviceIntent.putExtra(QueryBuilder.SHUTDOWN_CMD, true);
			context.startService(serviceIntent);
		}
	}
}