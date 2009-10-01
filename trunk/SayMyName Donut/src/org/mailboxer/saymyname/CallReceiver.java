package org.mailboxer.saymyname;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences preferences = context.getSharedPreferences("saymyname", Context.MODE_WORLD_WRITEABLE);
		if(!preferences.getBoolean("start", false)) {
			return;
		}

		context.startService(new Intent(context, SpeakService.class));
	}
}