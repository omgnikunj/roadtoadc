package org.mailboxer.saymyname;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;

public class Caller {
	public String UNKNOWN = "unknown";

	private String name;
	private String number;
	private String type = "";
	private Context context;
	private Settings settings;

	public Caller(Context context, String incomingNumber, Settings settings) {
		this.settings = settings;
		this.context = context;
		number = incomingNumber;
		UNKNOWN = context.getResources().getString(R.string.caller_unknown);

		resolveNumber(incomingNumber);
	}

	private void resolveNumber(String incomingNumber) {
		// safety first
		if (incomingNumber == null) {
			name = UNKNOWN;
			return;
		}

		if (incomingNumber.equals("")) {
			name = UNKNOWN;
			return;
		}

		// number-lookup
		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(incomingNumber));
		Cursor cur = context.getContentResolver().query(contactUri, new String[] {People.NAME, People.TYPE, People._ID}, null, null, null);

		if (cur.moveToFirst()) {
			if (cur.getString(0) != null) {
				// get contact's name
				name = cur.getString(0);

				// get number's type:
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
					// maybe a custom type
					type = "";
					break;
				}
			} else {
				// couldn't find a contact for that number
				name = number;
				type = "";
				return;
			}
		} else {
			// not really needed, but let's go the safe way
			name = number;
			type = "";
			return;
		}

		try {
			// if there's a file in app's private directory don't read!
			// (doesn't matter what's in the file, is created by ContactChooser.java)
			context.openFileInput(cur.getString(2));

			// stop the main-service, because there's nothing to do
			Intent serviceIntent = new Intent(context, QueryBuilder.class);
			serviceIntent.putExtra(QueryBuilder.STOP_CMD, true);
			context.startService(serviceIntent);

			// not really needed, but let's go the safe way
			name = "";
			type = "";
		} catch (FileNotFoundException e) {}
	}

	public String buildString(String formatString) {
		// safety first
		if (name == null) {
			return UNKNOWN;
		}
		if (name.equals("")) {
			return UNKNOWN;
		}

		// incoming number not in addressbook - read number
		if (name.equals(number)) {
			if (name.matches("^[+]\\d+||\\d+")) {
				// it is a number - read it or "unknown" ?
				if (settings.isReadNumber()) {
					// read the number
					char[] temp = number.toCharArray();
					name = "";
					for (int i = 0; i < temp.length; i++) {
						name += temp[i] + " ";
					}
					return name;
				} else {
					return UNKNOWN;
				}
			} else {
				// it isn´t a number, maybe anything special that has a name - read it
				return name;
			}
		}

		if (settings.isCutName()) {
			// user doesn't want to hear the whole name
			name = name.split(" ")[0];
		}

		// look for % and & - and replace them
		// (at least % has to be available, this is guaranteed by SayMyName.java)
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