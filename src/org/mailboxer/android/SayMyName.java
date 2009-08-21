package org.mailboxer.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.tts.ConfigurationManager;

public class SayMyName extends Activity {
	Button btnTTS;
	Button btnSeconds;
	Button btnRingdroid;
	Button btnTest;

	EditText editRepeat;

	private final String PATH = "/sdcard/.saymyname";
	private final String FILE = "repeatPref.txt";


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if(isInstalled(this)) {
			startService(new Intent(SayMyName.this, SpeakService.class));
		} else {
			if(checkTtsRequirements(this)) {
				startService(new Intent(SayMyName.this, SpeakService.class));
			} else {
				// EPIC FAIL!
			}
		}

		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setMode(AudioManager.STREAM_MUSIC);
		for(int i = audio.getStreamVolume(AudioManager.STREAM_MUSIC); i < audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC); i++) {
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE);
		}

		editRepeat = (EditText) findViewById(R.id.editRepeat);

		File datei = new File(PATH + "/" + FILE);
		if(!datei.exists()) {
			try {
				new File(PATH).mkdirs();
				datei.createNewFile();

				FileWriter writer = null;
				BufferedWriter buffWriter = null;

				try {
					writer = new FileWriter(PATH + "/" + FILE);
					buffWriter = new BufferedWriter(writer);

					buffWriter.write("0");

					buffWriter.close();
					writer.close();
				} catch (IOException e) {}
			} catch (IOException e) {}
		} else {
			FileReader reader = null;
			BufferedReader buffReader = null;

			try {
				reader = new FileReader(PATH + "/" + FILE);
				buffReader = new BufferedReader(reader);

				String s = buffReader.readLine();

				editRepeat.setText(s);
			} catch (IOException e) {
			} finally {
				try {
					reader.close();
				} catch (IOException e) {}
				try {
					buffReader.close();
				} catch (IOException e) {}
			}
		}

		btnSeconds = (Button) findViewById(R.id.btnSeconds);
		btnSeconds.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				FileWriter writer = null;
				BufferedWriter buffWriter = null;

				if(editRepeat.getText().toString() != null) {
					try {
						writer = new FileWriter(PATH + "/" + FILE);
						buffWriter = new BufferedWriter(writer);

						buffWriter.write(editRepeat.getText().toString());

						buffWriter.close();
						writer.close();

						// requirements
						startService(new Intent(SayMyName.this, SpeakService.class));

					} catch (IOException e) {}
				} else {
					editRepeat.setText("0");
				}
			}
		});


		btnTest = (Button) findViewById(R.id.btnTest);
		btnTest.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent speakIntent = new Intent(SayMyName.this, SpeakService.class);
				speakIntent.putExtra("say", "I hope you enjoy my app");

				startService(speakIntent);
			}
		});

		btnTTS = (Button) findViewById(R.id.btnTTS);
		btnTTS.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intentTTS = new Intent();
				intentTTS.setComponent(new ComponentName("com.google.tts", "com.google.tts.ConfigurationManager"));

				try {
					startActivity(intentTTS);
				} catch(Exception e) {
					Intent intentMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=library pub:\"Charles Chen\""));
					startActivity(intentMarket);
				}
			}
		});

		btnRingdroid = (Button) findViewById(R.id.btnRingdroid);
		btnRingdroid.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intentRingdroid = new Intent("android.intent.action.GET_CONTENT");

				Uri ringtone = RingtoneManager.getActualDefaultRingtoneUri(SayMyName.this, RingtoneManager.TYPE_RINGTONE);

				intentRingdroid.setDataAndType(ringtone, "audio/");

				try {
					startActivity(intentRingdroid);
				} catch(Exception e) {
					Intent intentMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=Ringdroid pub:\"Ringdroid Team\""));

					startActivity(intentMarket);
				}
			}
		});
	}

	public static boolean isInstalled(Context ctx){
		try {
			Context myContext = ctx.createPackageContext("com.google.tts", 0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

	private boolean checkTtsRequirements(Activity activity) {
		int resultCode = 42;

		if (!isInstalled(activity)) {
			Uri marketUri = Uri.parse("market://search?q=pname:com.google.tts");
			Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			activity.startActivityForResult(marketIntent, resultCode);
			return false;
		}

		if (!ConfigurationManager.allFilesExist()) {
			int flags = Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY;
			Context myContext;

			try {
				myContext = createPackageContext("com.google.tts", flags);
				Class<?> appClass = myContext.getClassLoader().loadClass("com.google.tts.ConfigurationManager");
				Intent intent = new Intent(myContext, appClass);
				startActivityForResult(intent, resultCode);
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