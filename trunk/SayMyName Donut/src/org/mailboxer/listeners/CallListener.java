package org.mailboxer.listeners;

import org.mailboxer.saymyname.QueryBuilder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			Log.e("SMN", "START");

			Intent serviceIntent = new Intent(context, QueryBuilder.class);
			serviceIntent.putExtra(QueryBuilder.COMMUNICATION_TYPE, QueryBuilder.COMMUNICATION_CALL);
			serviceIntent.putExtra(QueryBuilder.NUMBER, intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
			context.startService(serviceIntent);
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
			Log.e("SMN", "STOP");

			Intent serviceIntent = new Intent(context, QueryBuilder.class);
			serviceIntent.putExtra(QueryBuilder.STOP_CMD, true);
			context.startService(serviceIntent);
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			Log.e("SMN", "SHUTDOWN");

			Intent serviceIntent = new Intent(context, QueryBuilder.class);
			serviceIntent.putExtra(QueryBuilder.SHUTDOWN_CMD, true);
			context.startService(serviceIntent);
		}
	}
}