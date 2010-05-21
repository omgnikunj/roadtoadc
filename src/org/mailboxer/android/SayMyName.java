package org.mailboxer.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.widget.Toast;

import com.google.tts.ConfigurationManager;

public class SayMyName extends PreferenceActivity {
	// from:
	// http://android-developers.blogspot.com/2009/09/introduction-to-text-to-speech-in.html
	private static final int ringtoneCheckReqCode = 56;

	private static final int ttsCheckReqCode = 42;

	public static boolean isInstalled(final Context ctx) {
		try {
			ctx.createPackageContext("com.google.tts", 0);
		} catch (final NameNotFoundException e) {
			return false;
		}
		return true;
	}

	private PreferenceScreen screen;

	private boolean checkTtsRequirements(final Activity activity, final int resultCode) {
		if (!isInstalled(activity)) {
			final Uri marketUri = Uri.parse("market://search?q=pname:com.google.tts");
			final Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			activity.startActivityForResult(marketIntent, resultCode);

			return false;
		}

		if (!ConfigurationManager.allFilesExist()) {
			try {
				final Context myContext = activity.createPackageContext("com.google.tts", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
				final Class<?> appClass = myContext.getClassLoader().loadClass("com.google.tts.ConfigurationManager");

				activity.startActivityForResult(new Intent(myContext, appClass), resultCode);
			} catch (final NameNotFoundException e) {} catch (final ClassNotFoundException e) {}

			return false;
		}

		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == ttsCheckReqCode) {
			if (checkTtsRequirements(this, ttsCheckReqCode)) {
				Toast.makeText(SayMyName.this, R.string.tts_check_toast, Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("org.mailboxer.saymyname");
		addPreferencesFromResource(R.xml.preferences);

		// check for TTS installed
		onActivityResult(ttsCheckReqCode, 0, null);

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
				if (key.equals("callerFormat") || key.equals("smsFormat") || key.equals("emailFormat")) {
					// be sure that there's at least % in the
					// formatstring
					final String changedFormat = sharedPreferences.getString(key, null);

					if (!changedFormat.contains("%")) {
						final SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString(key, "%");
						editor.commit();
					}
				}
			}
		});

		screen = getPreferenceScreen();

		screen.findPreference("ringtone").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				final SharedPreferences shared = preference.getSharedPreferences();
				if (shared.getBoolean(preference.getKey(), false)) {
					if (shared.getString("ringtoneUri", "").equals("")) {
						Toast.makeText(SayMyName.this, "Please wait", Toast.LENGTH_SHORT).show();
						// can't find silent ringtone's URI - save the
						// silent ringtone on sdcard and put it in the
						// ringtone-database
						final File dir = new File("/sdcard/media/audio/ringtones");
						final File file = new File("/sdcard/media/audio/ringtones/silent.mp3");

						if (!file.exists()) {
							dir.mkdirs();

							try {
								final InputStream input = getResources().openRawResource(R.raw.silent);
								final FileOutputStream output = new FileOutputStream(file);

								int bytal;
								do {
									bytal = input.read();
									output.write(bytal);
								} while (bytal != -1);

								input.close();
								output.close();
							} catch (final FileNotFoundException e) {} catch (final IOException e) {}
						}

						// from:
						// http://stackoverflow.com/questions/1271777/how-to-set-ringtone-in-android-from-my-activity
						final ContentValues values = new ContentValues();
						values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
						values.put(MediaStore.MediaColumns.TITLE, "Silent");
						values.put(MediaStore.MediaColumns.SIZE, 18432);
						values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
						values.put(AudioColumns.ARTIST, "TomTasche");
						values.put(AudioColumns.ALBUM, "SayMyName");
						values.put(AudioColumns.DURATION, 1071);
						values.put(AudioColumns.IS_RINGTONE, true);
						values.put(AudioColumns.IS_NOTIFICATION, true);
						values.put(AudioColumns.IS_ALARM, false);
						values.put(AudioColumns.IS_MUSIC, false);

						// Insert it into the database
						final Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
						final Uri newUri = getContentResolver().insert(uri, values);
						RingtoneManager.setActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE, newUri);

						final SharedPreferences.Editor editor = shared.edit();
						editor.putString("ringtoneUri", newUri.toString());
						editor.commit();

						Toast.makeText(SayMyName.this, SayMyName.this.getString(R.string.preference_ringtone_toast_finished), Toast.LENGTH_LONG).show();
					} else {
						// found silent ringtone's URI - set it as
						// default ringtone
						RingtoneManager.setActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE, Uri.parse(shared.getString("ringtoneUri", "")));

						Toast.makeText(SayMyName.this, SayMyName.this.getString(R.string.preference_ringtone_toast_finished), Toast.LENGTH_LONG).show();
					}
				} else {
					// disable silent ringtone and let the user choose
					// another one
					startActivityForResult(new Intent(RingtoneManager.ACTION_RINGTONE_PICKER), ringtoneCheckReqCode);
				}

				return false;
			}
		});

		screen.findPreference("tts").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				// start TTS Settings
				startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
				Toast.makeText(SayMyName.this, SayMyName.this.getString(R.string.preference_tts_toast), Toast.LENGTH_LONG).show();

				return false;
			}
		});

		screen.findPreference("ringdroid").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				// start Ringdroid
				final Intent intentRingdroid = new Intent("android.intent.action.GET_CONTENT");
				final Uri ringtone = RingtoneManager.getActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE);
				intentRingdroid.setDataAndType(ringtone, "audio/");

				try {
					startActivity(intentRingdroid);
				} catch (final ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=Ringdroid pub:\"Ringdroid Team\"")));
				}

				return false;
			}
		});

		screen.findPreference("locale").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				// start Locale
				final Intent intentLocale = new Intent();
				intentLocale.setComponent(new ComponentName("edu.mit.locale", "edu.mit.locale.ui.activities.Locale"));

				try {
					startActivity(intentLocale);
				} catch (final ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:edu.mit.locale")));
				}

				return false;
			}
		});

		screen.findPreference("contactChooser").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				// start ContactChooser.java
				startActivity(new Intent(SayMyName.this, ContactChooser.class));

				return false;
			}
		});

		screen.findPreference("trouble").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				// view troubleshooting-page
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/roadtoadc/wiki/Troubleshooting")));

				return false;
			}
		});

		screen.findPreference("translate").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				// view translate-page
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/roadtoadc/wiki/TranslateMe")));

				return false;
			}
		});

		screen.findPreference("blog").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				// view blog
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://roadtoadc.blogspot.com/")));

				return false;
			}
		});

		screen.findPreference("donate").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				// install Donate version :D
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:org.mailboxer.saymyname.donate")));

				Toast.makeText(SayMyName.this, getString(R.string.preference_donate_toast), Toast.LENGTH_LONG).show();

				return false;
			}
		});
	}
}