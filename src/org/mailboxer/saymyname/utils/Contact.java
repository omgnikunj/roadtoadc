package org.mailboxer.saymyname.utils;

import java.io.FileNotFoundException;

import org.mailboxer.saymyname.R;
import org.mailboxer.saymyname.services.ManagerService;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Contacts.PeopleColumns;
import android.provider.ContactsContract.PhoneLookup;

public abstract class Contact {
	private static class DonutContact extends Contact {
		public DonutContact(final String incomingNumber, final Context context) {
			number = incomingNumber;
			this.context = context;

			resolveNumber();
		}

		@Override
		protected void resolveNumber() {
			final Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(number));
			final Cursor cur = context.getContentResolver().query(contactUri, new String[] {PeopleColumns.NAME}, null, null, null);

			if (cur.moveToFirst()) {
				if (cur.getString(0) != null) {
					name = cur.getString(0);
				} else {
					name = UNKNOWN;
				}
			} else {
				name = UNKNOWN;
			}
		}
	}

	private static class EclairContact extends Contact {
		public EclairContact(final String incomingNumber, final Context context) {
			number = incomingNumber;
			this.context = context;

			resolveNumber();
		}

		@Override
		protected void resolveNumber() {
			final Cursor cur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.NUMBER + " = " + number, null, null);

			final int column = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

			if (cur.moveToFirst()) {
				if (cur.getString(column) != null) {
					name = cur.getString(column);
				} else {
					name = UNKNOWN;
				}
			} else {
				name = UNKNOWN;
			}
		}
	}

	private static class SpartanContact extends Contact {
		public SpartanContact(final String incomingNumber, final Context context) {
			number = incomingNumber;
			this.context = context;

			resolveNumber();
		}

		@Override
		protected void resolveNumber() {
			final Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
			final Cursor cur = context.getContentResolver().query(uri, null, null, null, null);

			final int column = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

			if (cur.moveToFirst()) {
				if (cur.getString(column) != null) {
					name = cur.getString(column);
				} else {
					name = UNKNOWN;
				}
			} else {
				name = UNKNOWN;
			}
		}
	}

	public static String UNKNOWN = "unknown";

	public static String getCaller(final String incomingNumber, final Context context, final Settings settings) {
		UNKNOWN = context.getResources().getString(R.string.caller_unknown);

		String name = null;
		String text = null;
		Contact contact = null;

		if (incomingNumber.matches("^[+]\\d+||\\d+")) {
			if (Integer.parseInt(Build.VERSION.SDK) == Build.VERSION_CODES.DONUT) {
				contact = new DonutContact(incomingNumber, context);
				text = contact.getName();

				name = text;
			} else {
				contact = new EclairContact(incomingNumber, context);
				text = contact.getName();

				if (text.equals(UNKNOWN)) {
					contact = new DonutContact(incomingNumber, context);
					text = contact.getName();

					if (text.equals(UNKNOWN)) {
						contact = new SpartanContact(incomingNumber, context);
						text = contact.getName();
					}
				}

				name = text;
			}
		} else {
			// it isn´t a number, maybe anything special that has a name - read
			// it
			name = incomingNumber;

			if (name.contains("\"")) {
				name = name.substring(name.indexOf('"') + 1, name.length());
				name = name.substring(0, name.indexOf('"'));
			}
		}

		if (name.equals(UNKNOWN)) {
			if (settings.isReadNumber()) {
				return incomingNumber;
			} else if (settings.isReadUnknown()) {
				return UNKNOWN;
			} else {
				context.stopService(new Intent(context, ManagerService.class));
				return "";
			}
		}

		if (name.equals("") || name == null) {
			context.stopService(new Intent(context, ManagerService.class));
			return "";
		}

		try {
			context.openFileInput(name);

			context.stopService(new Intent(context, ManagerService.class));
			return "";
		} catch (final FileNotFoundException e) {}

		final Formatter format = new Formatter(name, settings);
		name = format.format();

		if (name.equals("") || name == null) {
			context.stopService(new Intent(context, ManagerService.class));
			return "";
		}

		return name;
	}

	protected Context context;

	protected String name;

	// CupcakeContact?

	protected String number;

	private String getName() {
		return name;
	}

	protected abstract void resolveNumber();
}