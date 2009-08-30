package org.mailboxer.saymyname;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.google.tts.ConfigurationManager;
import com.google.tts.TTS;

public class SayMyName extends PreferenceActivity {
	public static Activity activity;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("saymyname");
		addPreferencesFromResource(R.xml.preferences);


		// check for TTS installed and start
		onActivityResult(ttsCheckReqCode, 0, null);
		startService(new Intent(SayMyName.this, SpeakService.class));

		setVolumeControlStream(AudioManager.STREAM_MUSIC);


		PreferenceScreen screen = getPreferenceScreen();

		Preference prefTest = screen.findPreference("test");
		prefTest.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				// start test-speak
				Intent speakIntent = new Intent(SayMyName.this, SpeakService.class);
				speakIntent.putExtra("say", "I hope you enjoy my app");
				startService(speakIntent);

				Toast.makeText(SayMyName.this, "Didn´t hear? Make sure phone isn´t silent and press again.", Toast.LENGTH_LONG).show();

				return false;
			}
		});

		Preference prefTTS = screen.findPreference("tts");
		prefTTS.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				// start TTS-preferences
				Intent intentTTS = new Intent();
				intentTTS.setComponent(new ComponentName("com.google.tts", "com.google.tts.ConfigurationManager"));

				try {
					startActivity(intentTTS);
				} catch(Exception e) {
					Intent intentMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=library pub:\"Charles Chen\""));
					startActivity(intentMarket);

					Toast.makeText(SayMyName.this, "You have to install TTS (Text-to-Speech-Library) to use this app.", Toast.LENGTH_LONG).show();
				}

				return false;
			}
		});

		Preference prefRingdroid = screen.findPreference("ringdroid");
		prefRingdroid.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				// start Ringdroid
				Intent intentRingdroid = new Intent("android.intent.action.GET_CONTENT");
				Uri ringtone = RingtoneManager.getActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE);
				intentRingdroid.setDataAndType(ringtone, "audio/");

				try {
					createPackageContext("com.ringdroid", 0);

					startActivity(intentRingdroid);
				} catch (NameNotFoundException e) {
					Intent intentMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=Ringdroid pub:\"Ringdroid Team\""));
					startActivity(intentMarket);

					Toast.makeText(SayMyName.this, "Ringdroid allows you to edit your ringtone and other music-files.", Toast.LENGTH_LONG).show();
				}

				return false;
			}
		});

		Preference prefTrouble = screen.findPreference("trouble");
		prefTrouble.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				// view troubleshooting-page
				Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/roadtoadc/wiki/Troubleshooting"));
				startActivity(sendIntent);

				return false;
			}
		});

		Preference prefHelp = screen.findPreference("problem");
		prefHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				// send mail
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with SayMyName");
				sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"tomtasche@gmail.com"});
				sendIntent.setType("message/rfc822");
				startActivity(sendIntent);

				Toast.makeText(SayMyName.this, "Thanks for your feedback!", Toast.LENGTH_LONG).show();

				return false;
			}
		});

		Preference prefBlog = screen.findPreference("blog");
		prefBlog.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				// view blog
				Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://roadtoadc.blogspot.com/"));
				startActivity(sendIntent);

				return false;
			}
		});
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

	private boolean checkTtsRequirements(Activity activity, int resultCode) {
		if (!TTS.isInstalled(activity)) {
			Uri marketUri = Uri.parse("market://search?q=pname:com.google.tts");
			Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			activity.startActivityForResult(marketIntent, resultCode);

			Toast.makeText(SayMyName.this, "You have to install TTS (Text-to-Speech-Library) to use this app.", Toast.LENGTH_LONG).show();

			return false;
		}
		if (!ConfigurationManager.allFilesExist()) {
			int flags = Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY;
			Context myContext;
			try {
				myContext = activity.createPackageContext("com.google.tts", flags);
				Class<?> appClass = myContext.getClassLoader().loadClass("com.google.tts.ConfigurationManager");
				Intent intent = new Intent(myContext, appClass);
				activity.startActivityForResult(intent, resultCode);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}