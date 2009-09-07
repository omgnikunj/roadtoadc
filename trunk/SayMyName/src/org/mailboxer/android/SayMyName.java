package org.mailboxer.android;

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

public class SayMyName extends PreferenceActivity {
	public static Activity activity;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("saymyname");
		addPreferencesFromResource(R.xml.preferences);


		// check for TTS installed and start
		onActivityResult(ttsCheckReqCode, 0, null);


		setVolumeControlStream(AudioManager.STREAM_MUSIC);


		PreferenceScreen screen = getPreferenceScreen();

		screen.findPreference("test").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// start test-speak
				Intent speakIntent = new Intent(SayMyName.this, SpeakService.class);
				speakIntent.putExtra("say", "I hope you enjoy my app");
				startService(speakIntent);

				Toast.makeText(SayMyName.this, "Didn´t hear? Make sure phone isn´t silent and press again.", Toast.LENGTH_LONG).show();

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

					Toast.makeText(SayMyName.this, "Locale controls your preferences based on your location", Toast.LENGTH_LONG).show();
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
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=library pub:\"Charles Chen\"")));

					Toast.makeText(SayMyName.this, "You have to install TTS (Text-to-Speech-Library) to use this app.", Toast.LENGTH_LONG).show();
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

					Toast.makeText(SayMyName.this, "Ringdroid allows you to edit your ringtone and other music-files.", Toast.LENGTH_LONG).show();
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

				Toast.makeText(SayMyName.this, "Thanks for your feedback!", Toast.LENGTH_LONG).show();

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

			Toast.makeText(SayMyName.this, "You have to install TTS (Text-to-Speech-Library) to use this app.", Toast.LENGTH_LONG).show();

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