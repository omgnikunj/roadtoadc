package org.mailboxer.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Contacts.People;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.tts.ConfigurationManager;
import com.google.tts.TTS;

public class SpeakService extends Service {
	private TTS.InitListener ttsInitListener = new TTS.InitListener() {

		public void onInit(int arg0) {}
	};
	private TTS talker;

	private boolean repeat;
	private int repeatSeconds;

	private String caller;

	private final String PATH = "/sdcard/.saymyname";
	private final String FILE = "repeatPref.txt";

	private TelephonyManager telephonyManager;

	private SpeakThread thread = new SpeakThread();


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// Test-Speak?
		Bundle extras = intent.getExtras();

		if(extras != null) {
			doSpeak(extras.getString("say"));
		}

		// first start
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

				repeatSeconds = 0;
				repeat = false;
			} catch (IOException e) {}
		}

		// normal start
		FileReader reader = null;
		BufferedReader buffReader = null;

		try {
			reader = new FileReader(PATH + "/" + FILE);
			buffReader = new BufferedReader(reader);

			String s = buffReader.readLine();

			repeatSeconds = Integer.parseInt(s);

			if(repeatSeconds > 0) {
				repeat = true;
			}
		} catch (IOException e) {
		} finally {
			try {
				reader.close();
			} catch (IOException e) {}
			try {
				buffReader.close();
			} catch (IOException e) {}
		}

		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		if(isInstalled(this)) {
			startService(new Intent(this, SpeakService.class));
		} else {
			if(checkTtsRequirements()) {
				startService(new Intent(this, SpeakService.class));
			} else {
				// EPIC FAIL!
			}
		}

		PhoneStateListener phoneListener = new PhoneStateListener() {

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if(state == TelephonyManager.CALL_STATE_RINGING) {
					startSpeaking(getCallerID(incomingNumber));
				}

				super.onCallStateChanged(state, incomingNumber);
			}
		};

		telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		talker = new TTS(this, ttsInitListener, true);

		super.onCreate();
	}

	// http://www.kluit.com/blog/index.php?/archives/10-Android-Example-A-contact-list-+-using-the-Phone.html
	// http://androidcommunity.com/forums/f3/cant-retrieve-the-data-from-contacts-7230/
	private String getCallerID(String incomingNumber) {
		String[] projection = new String[] {
				People.NAME,
				People.NUMBER,
		};

		Cursor c = getContentResolver().query(People.CONTENT_URI, projection, null, null, null);

		String result = null;

		if (c.moveToFirst()) {
			do {
				if(c.getString(1) != null) {
					if(c.getString(1).equals(incomingNumber)) {
						result = c.getString(0);
					}
				}
			} while (c.moveToNext());
		}

		return result;
	}

	private void startSpeaking(String caller) {
		this.caller = caller;

		if(repeat) {
			thread.run();
		} else {
			doSpeak(this.caller);
		}
	}

	private void doSpeak(String text) {
		talker.speak(text, 0, null);
	}


	public static boolean isInstalled(Context ctx){
		try {
			ctx.createPackageContext("com.google.tts", 0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

	private boolean checkTtsRequirements() {
		if (!isInstalled(this)) {
			Uri marketUri = Uri.parse("market://search?q=pname:com.google.tts");
			Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			startActivity(marketIntent);
			return false;
		}

		if (!ConfigurationManager.allFilesExist()) {
			int flags = Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY;
			Context myContext;

			try {
				myContext = createPackageContext("com.google.tts", flags);
				Class<?> appClass = myContext.getClassLoader().loadClass("com.google.tts.ConfigurationManager");
				Intent intent = new Intent(myContext, appClass);
				startActivity(intent);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	class SpeakThread extends Thread {

		@Override
		public void run() {
			do {
				doSpeak(caller);		

				try {
					sleep(repeatSeconds * 1000);
				} catch (InterruptedException e) {}
			} while(telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING);
		}
	}
}