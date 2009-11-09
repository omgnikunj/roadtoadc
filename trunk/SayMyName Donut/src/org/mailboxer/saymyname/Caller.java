package org.mailboxer.saymyname;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;

public class Caller {
	public static String UNKNOWN = "unknown";

	private String name;
	private String number;
	private String type = "";
	private Context context;
	private Settings settings;

	public Caller(Context context, String incomingNumber, Settings settings) {
		this.settings = settings;
		this.context = context;
		number = incomingNumber;
		// UNKNOWN = context.getResources().getString(R.string.caller_unknown);

		resolveNumber(incomingNumber);
	}

	private void resolveNumber(String incomingNumber) {
		if (incomingNumber == null) {
			name = UNKNOWN;
			return;
		}

		if (incomingNumber.equals("")) {
			name = UNKNOWN;
			return;
		}

		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(incomingNumber));
		Cursor cur = context.getContentResolver().query(contactUri, new String[] {People.NAME, People.TYPE, People._ID}, null, null, null);

		if (cur.moveToFirst()) {
			if (cur.getString(0) != null) {
				name = cur.getString(0);

				switch (Integer.parseInt(cur.getString(1))) {
				case People.TYPE_MOBILE:
					type = "Mobile";
					break;

				case People.TYPE_HOME:
					type = "Home";
					break;

				case People.TYPE_WORK:
					type = "Work";
					break;

				case People.TYPE_PAGER:
					type = "Pager";
					break;

				default:
					type = "";
					break;
				}
			} else {
				name = UNKNOWN;
				type = "";
				return;
			}
		} else {
			name = UNKNOWN;
			type = "";
			return;
		}

		try {
			context.openFileInput(cur.getString(2));

			Intent serviceIntent = new Intent(context, QueryBuilder.class);
			serviceIntent.putExtra(QueryBuilder.SHUTDOWN_CMD, true);
			context.startService(serviceIntent);

			name = "";
			type = "";
		} catch (FileNotFoundException e) {}
	}

	public String buildString(String formatString) {
		if (name == null) {
			return UNKNOWN;
		}
		if (name == "") {
			return UNKNOWN;
		}

		if (settings.isCutName()) {
			name = name.split(" ")[0];
		}

		String speech = formatString.replaceFirst("%", name);
		speech = speech.replaceFirst("&", type);

		return speech;
	}

	public String getName() {
		return name;
	}

	public String getNumber() {
		return number;
	}

	public String getType() {
		return type;
	}
}