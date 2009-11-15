package org.mailboxer.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
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
import android.widget.Toast;

import com.google.tts.ConfigurationManager;

public class SayMyName extends PreferenceActivity {
	public static Activity activity;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("saymyname");
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e1) {}

		if(!preferences.contains("firstStart")) {
			displayDialog();

			SharedPreferences.Editor editorPreference = preferences.edit();
			editorPreference.putBoolean("firstStart", false);
			editorPreference.putInt("versionCode", info.versionCode);
			editorPreference.commit();
		} else {
			if(info.versionCode > preferences.getInt("versionCode", 0)) {
				displayDialog();

				SharedPreferences.Editor editorPreference = preferences.edit();
				editorPreference.putInt("versionCode", info.versionCode);
				editorPreference.commit();
			}
		}

		if (Integer.parseInt(Build.VERSION.SDK) > 3) {
			displayUpgrade();
		}


		// check for TTS installed and start
		onActivityResult(ttsCheckReqCode, 0, null);


		setVolumeControlStream(AudioManager.STREAM_MUSIC);


		PreferenceScreen screen = getPreferenceScreen();

		CheckBoxPreference startPref = (CheckBoxPreference) screen.findPreference("start");
		startPref.setSummaryOff(R.string.saymyname_summary_on);
		startPref.setSummaryOn(R.string.saymyname_summary_on);

		startPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if(getPreferenceManager().getSharedPreferences().getBoolean("start", false)) {
					// start test-speak
					Intent speakIntent = new Intent(SayMyName.this, SpeakService.class);
					speakIntent.putExtra("say", getResources().getString(R.string.test_speak));
					startService(speakIntent);

					Toast.makeText(SayMyName.this, R.string.error_toast_install, Toast.LENGTH_LONG).show();
				} else {
					startService(new Intent(SayMyName.this, SpeakService.class));
					stopService(new Intent(SayMyName.this, SpeakService.class));
				}

				return false;
			}
		});

		CheckBoxPreference silentPref = (CheckBoxPreference) screen.findPreference("silent");
		silentPref.setSummaryOn(R.string.silent_on);
		silentPref.setSummaryOff(R.string.silent_off);

		screen.findPreference("ringtone").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
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

				return false;
			}
		});

		screen.findPreference("locale").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// start TTS-preferences
				Intent intentTTS = new Intent();
				intentTTS.setComponent(new ComponentName("edu.mit.locale", "edu.mit.locale.ui.activities.Locale"));

				try {
					startActivity(intentTTS);
				} catch(Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:edu.mit.locale")));

					Toast.makeText(SayMyName.this, R.string.locale_toast, Toast.LENGTH_LONG).show();
				}

				return false;
			}
		});

		screen.findPreference("tts").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// start TTS-preferences
				Intent intentTTS = new Intent();
				intentTTS.setComponent(new ComponentName("com.google.tts", "com.google.tts.ConfigurationManager"));

				try {
					startActivity(intentTTS);
				} catch(Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:com.google.tts")));

					Toast.makeText(SayMyName.this, R.string.tts_toast,Toast.LENGTH_LONG).show();
				}

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
					createPackageContext("com.ringdroid", 0);

					startActivity(intentRingdroid);
				} catch (NameNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=Ringdroid pub:\"Ringdroid Team\"")));

					Toast.makeText(SayMyName.this, R.string.ringdroid_toast, Toast.LENGTH_LONG).show();
				}

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

		screen.findPreference("problem").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// send mail
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with SayMyName");
				sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"tomtasche@gmail.com"});
				sendIntent.setType("message/rfc822");
				startActivity(sendIntent);

				Toast.makeText(SayMyName.this, R.string.feedback_toast, Toast.LENGTH_LONG).show();

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
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		startService(new Intent(this, SpeakService.class));

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	protected void onStop() {
		startService(new Intent(this, SpeakService.class));

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		startService(new Intent(this, SpeakService.class));

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// refresh UI here
		super.onResume();
	}

	private void displayDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);  
		dialog.setIcon(R.drawable.icon);
		dialog.setTitle(R.string.dialog_title);  
		dialog.setMessage(R.string.dialog_message);
		dialog.setPositiveButton(R.string.dialog_button, new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {}  
		});
		dialog.show();
	}

	private void displayUpgrade() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);  
		dialog.setIcon(R.drawable.icon);
		dialog.setTitle(getString(R.string.dialog_update_title));  
		dialog.setMessage(getString(R.string.dialog_update_text));
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pname:org.mailboxer.saymyname")));
			}
		});
		dialog.setNegativeButton("No", null);
		dialog.show();
	}


	// check for TTS
	// i know it´s not perfect, maybe i change that later

	// from:
	// http://groups.google.com/group/tts-for-android/browse_thread/thread/00f19431f01067b3?pli=1
	private static final int ttsCheckReqCode = 42;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ttsCheckReqCode) {
			if (checkTtsRequirements(this, ttsCheckReqCode)) {
				startService(new Intent(SayMyName.this, SpeakService.class));
			}
		}
	}

	public static boolean isInstalled(Context ctx){
		try {
			ctx.createPackageContext("com.google.tts", 0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

	private boolean checkTtsRequirements(Activity activity, int resultCode) {
		if (!isInstalled(activity)) {
			Uri marketUri = Uri.parse("market://search?q=pname:com.google.tts");
			Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			activity.startActivityForResult(marketIntent, resultCode);

			Toast.makeText(SayMyName.this, R.string.tts_toast, Toast.LENGTH_LONG).show();

			return false;
		}

		if (!ConfigurationManager.allFilesExist()) {
			try {
				Context myContext = activity.createPackageContext("com.google.tts", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
				Class<?> appClass = myContext.getClassLoader().loadClass("com.google.tts.ConfigurationManager");

				activity.startActivityForResult(new Intent(myContext, appClass), resultCode);
			} catch (NameNotFoundException e) {
			} catch (ClassNotFoundException e) {}

			return false;
		}

		return true;
	}
}