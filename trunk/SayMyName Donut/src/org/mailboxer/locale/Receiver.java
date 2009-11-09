package org.mailboxer.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public final class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (com.twofortyfouram.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
			boolean startCaller = intent.getBooleanExtra("org.mailboxer.android.extra.START_CALLER", false);
			boolean startSMS = intent.getBooleanExtra("org.mailboxer.android.extra.START_SMS", false);

			SharedPreferences.Editor editor = context.getSharedPreferences("saysomething", Context.MODE_WORLD_WRITEABLE).edit();

			editor.putBoolean("startSayCaller", startCaller);
			editor.putBoolean("startSaySMS", startSMS);
			editor.commit();
		}
	}
}