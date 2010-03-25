package org.mailboxer.listeners;

import org.mailboxer.saymyname.QueryBuilder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class K9MailReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		//			Log.e("com.fsck.k9.intent.extra.ACCOUNT", 
		//					intent.getStringExtra("com.fsck.k9.intent.extra.ACCOUNT"));
		//			Log.e("com.fsck.k9.intent.extra.FOLDER", 
		//					intent.getStringExtra("com.fsck.k9.intent.extra.FOLDER"));
		//			//Log.e("com.fsck.k9.intent.extra.SENT_DATE", 
		//			//		intent.get("com.fsck.k9.intent.extra.SENT_DATE"));
		//			Log.e("com.fsck.k9.intent.extra.FROM", 
		//					intent.getStringExtra("com.fsck.k9.intent.extra.FROM"));
		//			Log.e("com.fsck.k9.intent.extra.SUBJECT", 
		//					intent.getStringExtra("com.fsck.k9.intent.extra.SUBJECT"));

		Intent serviceIntent = new Intent(context, QueryBuilder.class);
		serviceIntent.putExtra(
				QueryBuilder.COMMUNICATION_TYPE, QueryBuilder.COMMUNICATION_EMAIL);
		serviceIntent.putExtra(
				QueryBuilder.EMAIL, intent.getStringExtra(
						"com.fsck.k9.intent.extra.FROM"));
		serviceIntent.putExtra(
				QueryBuilder.SUBJECT, intent.getStringExtra(
						"com.fsck.k9.intent.extra.SUBJECT"));
		context.startService(serviceIntent);
	}
}