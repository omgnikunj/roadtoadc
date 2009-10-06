package org.mailboxer.saymyname;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

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

		// check for TTS installed and start
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, ttsCheckReqCode);


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
				startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));

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
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with SayMyName Donut");
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

	private static final int ttsCheckReqCode = 42;
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ttsCheckReqCode) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				startService(new Intent(SayMyName.this, SpeakService.class));
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}
}