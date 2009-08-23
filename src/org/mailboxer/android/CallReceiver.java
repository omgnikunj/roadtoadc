package org.mailboxer.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {
	Context context;


	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		boolean installed;


		try {
			context.createPackageContext("com.google.tts", 0);

			installed = true;
		} catch (NameNotFoundException e) {
			Intent startIntent = new Intent(context, SayMyName.class);
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(startIntent);

			Toast.makeText(context, "You have to install TTS (Text-to-Speech-Library) to use this app.", Toast.LENGTH_LONG).show();

			installed = false;
		}


		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && installed) {
			new Thread() {
				public void run() {
					try {
						sleep(30000);
					} catch (InterruptedException e) {}

					bootService();
				};
			}.start();
		} else if(installed) {
			bootService();
		}
	}

	private void bootService() {
		context.startService(new Intent(context, SpeakService.class));
	}
}