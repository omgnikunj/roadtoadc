package org.mailboxer.listeners;

import org.mailboxer.saymyname.QueryBuilder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Object[] pdusObj = (Object[]) bundle.get("pdus");

		SmsMessage[] messages = new SmsMessage[pdusObj.length];
		for (int i = 0; i < pdusObj.length; i++) {
			messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
		}

		StringBuilder sb = new StringBuilder();

		if (messages.length > 1) {
			for (SmsMessage currentMessage: messages){
				sb.append(currentMessage.getDisplayMessageBody());
				sb.append('\n');
			}
		} else {
			sb.append(messages[0].getDisplayMessageBody());
		}

		Intent serviceIntent = new Intent(context, QueryBuilder.class);
		serviceIntent.putExtra(QueryBuilder.SHUTDOWN_CMD, false);
		serviceIntent.putExtra(QueryBuilder.COMMUNICATION_TYPE, QueryBuilder.COMMUNICATION_SMS);
		serviceIntent.putExtra(QueryBuilder.NUMBER, messages[0].getDisplayOriginatingAddress());
		serviceIntent.putExtra(QueryBuilder.MESSAGE, sb.toString());
		context.startService(serviceIntent);
	}
}