package org.mailboxer.locale;

import org.mailboxer.saymyname.SpeakService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public final class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (com.twofortyfouram.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
			boolean start = intent.getBooleanExtra("org.mailboxer.android.extra.START", false);
			int volume = intent.getIntExtra("org.mailboxer.android.extra.VOLUME", 7);
			boolean silent = intent.getBooleanExtra("org.mailboxer.android.extra.SILENT", true);
			String repeatTimeout = intent.getStringExtra("org.mailboxer.android.extra.TIMEOUT");
			String repeatTimes = intent.getStringExtra("org.mailboxer.android.extra.TIMES");
			

			SharedPreferences.Editor editor = context.getSharedPreferences("saymyname", Context.MODE_WORLD_WRITEABLE).edit();

			editor.putBoolean("start", start);
			editor.putBoolean("silent", silent);
			editor.putString("volume", Integer.toString(volume));
			editor.putString("repeatSeconds", repeatTimeout);
			editor.putString("repeatTimes", repeatTimes);
			editor.commit();


			context.startService(new Intent(context, SpeakService.class));
		}
	}
}