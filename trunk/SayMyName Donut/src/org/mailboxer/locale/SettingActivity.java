package org.mailboxer.locale;

import org.mailboxer.saymyname.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;


public class SettingActivity extends Activity {
	private boolean isCancelled = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.locale);

		// Set up the breadcrumbs in the titlebar
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.locale_ellipsizing_title);

		String breadcrumbString = getIntent().getStringExtra(com.twofortyfouram.Intent.EXTRA_STRING_BREADCRUMB);

		// Locale guarantees that the breadcrumb string will be present, but checking for null anyway makes your Activity more
		// robust and re-usable
		if (breadcrumbString == null) {
			breadcrumbString = getString(R.string.app_name);
		} else {
			breadcrumbString = String.format("%s%s%s", breadcrumbString, com.twofortyfouram.Intent.BREADCRUMB_SEPARATOR, getString(R.string.app_name)); //$NON-NLS-1$
		}

		((TextView) findViewById(R.id.locale_ellipsizing_title_text)).setText(breadcrumbString);
		setTitle(breadcrumbString); // although not actually necessary, this is helpful when starting sub-Activities

		// if savedInstanceState == null, then we are entering the Activity directly from Locale and we need to check whether the
		// Intent has data or is clean (e.g. whether we editing an old setting or creating a new one)
		if (savedInstanceState == null) {
			boolean start = getIntent().getBooleanExtra("org.mailboxer.android.extra.START", false);
			int volume = getIntent().getIntExtra("org.mailboxer.android.extra.VOLUME", 7);
			boolean silent = getIntent().getBooleanExtra("org.mailboxer.android.extra.SILENT", true);
			String repeatTimeout = getIntent().getStringExtra("org.mailboxer.android.extra.TIMEOUT");
			String repeatTimes = getIntent().getStringExtra("org.mailboxer.android.extra.TIMES");

			((SeekBar) findViewById(R.id.volumeSeek)).setMax(((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_MUSIC));

			if (repeatTimeout != null) {
				((CheckBox) findViewById(R.id.enableCheck)).setChecked(start);
				((EditText) findViewById(R.id.repeatTimeoutEdit)).setText(repeatTimeout);
				((EditText) findViewById(R.id.repeatTimesEdit)).setText(repeatTimes);
				((SeekBar) findViewById(R.id.volumeSeek)).setProgress(volume);
				((CheckBox) findViewById(R.id.silentCheck)).setChecked(silent);
			}
		}
		// if savedInstanceState != null, there is no need to restore any Activity state directly (e.g. onSaveInstanceState()).
		// This is handled by the TextView automatically.

		super.onCreate(savedInstanceState);
	}

	@Override
	public void finish() {
		if (isCancelled) {
			setResult(RESULT_CANCELED);
		} else {
			boolean start = ((CheckBox) findViewById(R.id.enableCheck)).isChecked();
			int volume = ((SeekBar) findViewById(R.id.volumeSeek)).getProgress();
			boolean silent = ((CheckBox) findViewById(R.id.silentCheck)).isChecked();
			String repeatTimeout = ((EditText) findViewById(R.id.repeatTimeoutEdit)).getText().toString();
			String repeatTimes = ((EditText) findViewById(R.id.repeatTimesEdit)).getText().toString();

			// This is the store-and-forward Intent to ourselves.
			final Intent returnIntent = new Intent();

			// this extra is the data to ourselves: either for the Activity or the BroadcastReceiver
			returnIntent.putExtra("org.mailboxer.android.extra.START", start);
			returnIntent.putExtra("org.mailboxer.android.extra.VOLUME", volume);
			returnIntent.putExtra("org.mailboxer.android.extra.SILENT", silent);
			returnIntent.putExtra("org.mailboxer.android.extra.TIMEOUT", repeatTimeout);
			returnIntent.putExtra("org.mailboxer.android.extra.TIMES", repeatTimes);


			String blurb;
			if(start) {
				blurb = getResources().getString(R.string.locale_plugin_enabled) + "; ";
			} else {
				blurb = getResources().getString(R.string.locale_plugin_disabled) + "; ";
			}

			blurb += "Level: " + volume + "; ";

			// this is the blurb shown in the Locale UI
			if (blurb.length() > com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH) {
				returnIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, blurb.substring(0, com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH));
			} else {
				returnIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, blurb);
			}

			setResult(RESULT_OK, returnIntent);
		}

		super.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_dontsave: {
			isCancelled = true;
			finish();
			return true;
		}

		case R.id.menu_save: {
			finish();
			return true;
		}

		case R.id.menu_help: {
			final Intent helpIntent = new Intent(com.twofortyfouram.Intent.ACTION_HELP);

			helpIntent.putExtra("com.twofortyfouram.locale.intent.extra.HELP_URL", "http://code.google.com/p/roadtoadc/wiki/LocalePluginHelp"); //$NON-NLS-1$ //$NON-NLS-2$

			// insert the breadcrumbs. Note: the title was set in onCreate
			helpIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BREADCRUMB, getTitle().toString());

			startActivity(helpIntent);
			return true;
		}
		default:
		{
			// we shouldn't ever fall through to this
		}
		}

		return super.onMenuItemSelected(featureId, item);
	}
}
