package org.mailboxer.saymyname;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public class SayMyName extends PreferenceActivity {
	public static Activity activity;
	private PreferenceScreen screen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("saysomething");
		addPreferencesFromResource(R.xml.preferences);

		if (Integer.parseInt(Build.VERSION.SDK) > 4) {
			displayUpgrade();
		}

		// check for TTS installed
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, ttsCheckReqCode);

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals("callerFormat") || key.equals("smsFormat")) {
					// be sure that there's at least % in the formatstring
					String changedFormat = sharedPreferences.getString(key, null);

					if (changedFormat.length() <= 1) {
						if (!changedFormat.contains("%")) {
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString(key, "%");
							editor.commit();
						}
					}
				}
			}
		});

		screen = getPreferenceScreen();

		screen.findPreference("ringtone").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences shared = preference.getSharedPreferences();
				if (shared.getBoolean(preference.getKey(), false)) {
					if (shared.getString("ringtoneUri", "").equals("")) {
						// can't find silent ringtone's URI - save the silent ringtone on sdcard and put it in the ringtone-database
						File dir = new File("/sdcard/media/audio/ringtones");
						File file = new File("/sdcard/media/audio/ringtones/silent.mp3");

						if(!file.exists()) {
							dir.mkdirs();

							try {
								InputStream input = getResources().openRawResource(R.raw.silent);
								FileOutputStream output = new FileOutputStream(file);

								int bytal;
								do {
									bytal = input.read();
									output.write(bytal);
								} while (bytal != -1);

								input.close();
								output.close();
							} catch (FileNotFoundException e) {
							} catch (IOException e) {}
						}

						// from: http://stackoverflow.com/questions/1271777/how-to-set-ringtone-in-android-from-my-activity
						ContentValues values = new ContentValues();
						values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
						values.put(MediaStore.MediaColumns.TITLE, "Silent");
						values.put(MediaStore.MediaColumns.SIZE, 18432);
						values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
						values.put(MediaStore.Audio.Media.ARTIST, "TomTasche");
						values.put(MediaStore.Audio.Media.ALBUM, "SayMyName");
						values.put(MediaStore.Audio.Media.DURATION, 1071);
						values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
						values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
						values.put(MediaStore.Audio.Media.IS_ALARM, false);
						values.put(MediaStore.Audio.Media.IS_MUSIC, false);

						// Insert it into the database
						Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
						Uri newUri = getContentResolver().insert(uri, values);
						RingtoneManager.setActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE, newUri);

						SharedPreferences.Editor editor = shared.edit();
						editor.putString("ringtoneUri", newUri.toString());
						editor.commit();

						Toast.makeText(SayMyName.this, SayMyName.this.getString(R.string.preference_ringtone_toast_finished), Toast.LENGTH_LONG).show();
					} else {
						// found silent ringtone's URI - set it as default ringtone
						RingtoneManager.setActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE, Uri.parse(shared.getString("ringtoneUri", "")));

						Toast.makeText(SayMyName.this, SayMyName.this.getString(R.string.preference_ringtone_toast_finished), Toast.LENGTH_LONG).show();
					}
				} else {
					// disable silent ringtone and let the user choose another one
					startActivityForResult(new Intent(RingtoneManager.ACTION_RINGTONE_PICKER), ringtoneCheckReqCode);
				}

				return false;
			}
		});

		screen.findPreference("tts").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// start TTS Settings
				startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
				Toast.makeText(SayMyName.this, SayMyName.this.getString(R.string.preference_tts_toast), Toast.LENGTH_LONG).show();

				return false;
			}
		});

		screen.findPreference("ringdroid").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// start Ringdroid
				Intent intentRingdroid = new Intent("android.intent.action.GET_CONTENT");
				Uri ringtone = RingtoneManager.getActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE);
				intentRingdroid.setDataAndType(ringtone, "audio/");

				try {
					startActivity(intentRingdroid);
				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=Ringdroid pub:\"Ringdroid Team\"")));
				}

				return false;
			}
		});

		screen.findPreference("locale").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// start Locale
				Intent intentLocale = new Intent();
				intentLocale.setComponent(new ComponentName("edu.mit.locale", "edu.mit.locale.ui.activities.Locale"));

				try {
					startActivity(intentLocale);
				} catch(ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:edu.mit.locale")));
				}

				return false;
			}
		});

		screen.findPreference("contactChooser").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// start ContactChooser.java
				startActivity(new Intent(SayMyName.this, ContactChooser.class));

				return false;
			}
		});

		screen.findPreference("trouble").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// view troubleshooting-page
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/roadtoadc/wiki/Troubleshooting")));

				return false;
			}
		});

		screen.findPreference("translate").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// view translate-page
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/roadtoadc/wiki/TranslateMe")));

				return false;
			}
		});

		screen.findPreference("blog").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// view blog
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://roadtoadc.blogspot.com/")));

				return false;
			}
		});

		screen.findPreference("donate").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// install Donate version :D
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:org.mailboxer.saymyname.donate")));

				Toast.makeText(SayMyName.this, getString(R.string.preference_donate_toast), Toast.LENGTH_LONG).show();

				return false;
			}
		});
	}

	private void displayUpgrade() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);  
		dialog.setIcon(R.drawable.icon);
		dialog.setTitle(getString(R.string.dialog_update_title));  
		dialog.setMessage(getString(R.string.dialog_update_text));
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:org.mailboxer.saymyname.eclair")));
			}
		});
		dialog.setNegativeButton("No", null);
		dialog.show();
	}

	// from:
	// http://android-developers.blogspot.com/2009/09/introduction-to-text-to-speech-in.html
	private static final int ringtoneCheckReqCode = 56;
	private static final int ttsCheckReqCode = 42;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ttsCheckReqCode) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				Toast.makeText(this, getString(R.string.tts_check_toast), Toast.LENGTH_SHORT).show();
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		} else if (requestCode == ringtoneCheckReqCode) {
			// get picked ringtone's uri and set it as default ringtone
			if (data != null) {
				RingtoneManager.setActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE, (Uri) data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI));
			} else {
				((CheckBoxPreference) screen.findPreference("ringtone")).setChecked(true);
			}
		}
	}
}