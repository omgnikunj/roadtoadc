package org.mailboxer.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CallReceiver extends BroadcastReceiver {
	Context context;


	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;

		bootService();
	}

	private void bootService() {
		context.startService(new Intent(context, SpeakService.class));
	}
}