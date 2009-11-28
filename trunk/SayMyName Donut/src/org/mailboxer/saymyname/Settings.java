package org.mailboxer.saymyname;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
	private int callerRepeatSeconds;
	private int callerRepeatTimes;

	private int smsReadDelay;
	private boolean smsRead;
	private int smsRepeatTimes;
	private boolean smsReadDiscreet;

	private boolean startSomething;
	private boolean startSayCaller;
	private boolean startSaySMS;

	private String callerFormatString;
	private String smsFormatString;

	private int wantedVolume;

	private boolean cutName;
	private boolean readNumber;

	private boolean discreetMode;


	public Settings(Context context) {
		// read and save a lot of settings
		SharedPreferences preferences = context.getSharedPreferences("saysomething", Context.MODE_WORLD_WRITEABLE);

		startSayCaller = preferences.getBoolean("saycaller", true);
		startSaySMS = preferences.getBoolean("saysms", true);
		startSomething = preferences.getBoolean("saysomething", true);

		if (!startSayCaller && !startSaySMS) {
			startSomething = false;
		}

		callerRepeatSeconds = Integer.parseInt(preferences.getString("callerRepeatSeconds", "3"));

		callerRepeatTimes = Integer.parseInt(preferences.getString("callerRepeatTimes", "4"));

		callerFormatString = preferences.getString("callerFormat", "");
		smsFormatString = preferences.getString("smsFormat", "");

		wantedVolume = Integer.parseInt(preferences.getString("wantedVolume", "5"));

		cutName = preferences.getBoolean("cutName", true);
		readNumber = preferences.getBoolean("readNumber", false);

		discreetMode = preferences.getBoolean("discreetMode", false);

		smsRead = preferences.getBoolean("smsRead", false);
		smsReadDelay = Integer.parseInt(preferences.getString("smsReadDelay", "3"));
		smsRepeatTimes = Integer.parseInt(preferences.getString("smsRepeatTimes", "1"));
		smsReadDiscreet = preferences.getBoolean("smsReadDiscreet", true);
	}

	public int getCallerRepeatSeconds() {
		return callerRepeatSeconds;
	}

	public int getCallerRepeatTimes() {
		return callerRepeatTimes;
	}

	public int getSmsReadDelay() {
		return smsReadDelay;
	}

	public boolean isStartSayCaller() {
		return startSayCaller;
	}

	public boolean isStartSaySMS() {
		return startSaySMS;
	}

	public boolean isStartSomething() {
		return startSomething;
	}

	public String getCallerFormatString() {
		return callerFormatString;
	}

	public String getSmsFormatString() {
		return smsFormatString;
	}

	public int getWantedVolume() {
		return wantedVolume;
	}

	public boolean isCutName() {
		return cutName;
	}

	public boolean isDiscreetMode() {
		return discreetMode;
	}

	public boolean isSmsRead() {
		return smsRead;
	}

	public boolean isSmsReadDiscreet() {
		return smsReadDiscreet;
	}

	public int getSmsRepeatTimes() {
		return smsRepeatTimes;
	}

	public boolean isReadNumber() {
		return readNumber;
	}
}