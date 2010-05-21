package org.mailboxer.saymyname.listeners;

import org.mailboxer.saymyname.services.ManagerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallListener extends BroadcastReceiver {
	private static boolean onCall;
	private Context context;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		this.context = context;

		final String newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

		if (newState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			Log.v("SMN", "ringing");

			if (onCall) {
				if (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() == TelephonyManager.CALL_STATE_IDLE) {
					onCall = false;
				} else {
					return;
				}
			}

			final Intent serviceIntent = new Intent(context, ManagerService.class);
			serviceIntent.putExtra("org.mailboxer.saymyname.number", intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
			context.startService(serviceIntent);
		} else if (newState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
			Log.v("SMN", "offhook");
			onCall = true;

			shutdown();
		} else if (newState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			Log.v("SMN", "idle");
			onCall = false;

			shutdown();
		}
	}

	private void shutdown() {
		final Intent serviceIntent = new Intent(context, ManagerService.class);
		context.stopService(serviceIntent);
	}
}