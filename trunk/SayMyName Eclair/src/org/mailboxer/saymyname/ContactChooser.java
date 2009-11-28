package org.mailboxer.saymyname;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.mailboxer.saymyname.eclair.R;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

// from: http://code.google.com/p/ringdroid/source/browse/trunk/src/com/ringdroid/ChooseContactActivity.java
public class ContactChooser extends ListActivity implements TextWatcher {
	private TextView mFilter;
	private SimpleCursorAdapter mAdapter;

	public ContactChooser() {}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Inflate our UI from its XML layout description.
		setContentView(R.layout.choose_contact);

		try {
			mAdapter = new SimpleCursorAdapter(
					this,
					// Use a template that displays a text view
					R.layout.contact_row,
					// Give the cursor to the list adatper
					createCursor(""),
					// Map from database columns...
					new String[] {
						People.CUSTOM_RINGTONE,
						People.STARRED,
						People.DISPLAY_NAME },
						// To widget ids in the row layout...
						new int[] {
						R.id.row_ringtone,
						R.id.row_starred,
						R.id.row_display_name });

			mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
				public boolean setViewValue(View view,
						Cursor cursor,
						int columnIndex) {
					String name = cursor.getColumnName(columnIndex);
					String value = cursor.getString(columnIndex);
					if (name.equals(People.CUSTOM_RINGTONE)) {
						if (value != null && value.length() > 0) {
							view.setVisibility(View.VISIBLE);
						} else  {
							view.setVisibility(View.INVISIBLE);
						}
						return true;
					}
					if (name.equals(People.STARRED)) {
						if (value != null && value.equals("1")) {
							view.setVisibility(View.VISIBLE);
						} else  {
							view.setVisibility(View.INVISIBLE);
						}
						return true;
					}

					return false;
				}
			});

			setListAdapter(mAdapter);

			// On click, assign ringtone to contact
			getListView().setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView parent,
						View view,
						int position,
						long id) {
					markContact();
				}
			});

		} catch (SecurityException e) {}

		mFilter = (TextView) findViewById(R.id.search_filter);
		if (mFilter != null) {
			mFilter.addTextChangedListener(this);
		}
	}

	private void markContact() {
		Cursor c = mAdapter.getCursor();

		int dataIndex = c.getColumnIndexOrThrow(People._ID);
		String contactId = c.getString(dataIndex);

		dataIndex = c.getColumnIndexOrThrow(People.DISPLAY_NAME);
		String displayName = c.getString(dataIndex);

		try {
			openFileInput(contactId);
			deleteFile(contactId);
			Toast.makeText(this, displayName + " unselected. I will announce him!", Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			try {
				FileOutputStream stream = openFileOutput(contactId, 0);
				stream.write(0);
				stream.close();
				Toast.makeText(this, displayName + " selected. I won´t announce him!", Toast.LENGTH_LONG).show();
			} catch (FileNotFoundException e1) {
			} catch (IOException e1) {}
		}

		finish();
		return;
	}

	private Cursor createCursor(String filter) {
		String selection;
		if (filter != null && filter.length() > 0) {
			selection = "(DISPLAY_NAME LIKE \"%" + filter + "%\")";
		} else {
			selection = null;
		}
		Cursor cursor = managedQuery(
				People.CONTENT_URI,
				new String[] {
						People._ID,
						People.CUSTOM_RINGTONE,
						People.DISPLAY_NAME,
						People.LAST_TIME_CONTACTED,
						People.NAME,
						People.STARRED,
						People.TIMES_CONTACTED },
						selection,
						null,
		"STARRED DESC, TIMES_CONTACTED DESC, LAST_TIME_CONTACTED DESC");

		return cursor;
	}

	public void afterTextChanged(Editable s) {
		String filterStr = mFilter.getText().toString();
		mAdapter.changeCursor(createCursor(filterStr));
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}
}