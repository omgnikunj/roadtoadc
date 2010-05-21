package org.mailboxer.saymyname.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
	private final String callerFormat;
	private final int callerRepeatSeconds;
	private final int callerRepeatTimes;

	private final boolean cutName;
	private final boolean cutNameAfterSpecialCharacters;
	private final int emailReadDelay;

	private final boolean emailReadSubject;
	private final String mailFormat;
	private final boolean readNumber;

	private final boolean readUnknown;
	private final String smsFormat;
	private final boolean smsRead;
	private final int smsReadDelay;

	private final String specialCharacters;
	private final boolean startSayCaller;
	private final boolean startSayEMail;
	private final boolean startSaySMS;
	private final boolean startSomething;

	public Settings(final Context context) {
		final SharedPreferences preferences = context.getSharedPreferences("org.mailboxer.saymyname", Context.MODE_WORLD_WRITEABLE);

		startSayCaller = preferences.getBoolean("saycaller", true);
		startSaySMS = preferences.getBoolean("saysms", true);
		startSayEMail = preferences.getBoolean("sayemail", true);
		startSomething = preferences.getBoolean("saysomething", true);

		callerRepeatSeconds = Integer.parseInt(preferences.getString("callerRepeatSeconds", "3")) * 1000;
		callerRepeatTimes = Integer.parseInt(preferences.getString("callerRepeatTimes", "6"));

		callerFormat = preferences.getString("callerFormat", "%");
		smsFormat = preferences.getString("smsFormat", "%");
		mailFormat = preferences.getString("emailFormat", "%");

		cutName = preferences.getBoolean("cutName", true);
		cutNameAfterSpecialCharacters = preferences.getBoolean("cutNameAfterSpecialCharacters", false);
		specialCharacters = preferences.getString("specialCharacters", ":/-(");
		readUnknown = preferences.getBoolean("readUnknown", true);
		readNumber = preferences.getBoolean("readNumber", false);

		smsRead = preferences.getBoolean("smsRead", false);
		smsReadDelay = Integer.parseInt(preferences.getString("smsReadDelay", "3")) * 1000;

		emailReadSubject = preferences.getBoolean("emailReadSubject", false);
		emailReadDelay = Integer.parseInt(preferences.getString("emailReadDelay", "2")) * 1000;
	}

	public String getCallerFormat() {
		return callerFormat;
	}

	public int getCallerRepeatSeconds() {
		return callerRepeatSeconds;
	}

	public int getCallerRepeatTimes() {
		return callerRepeatTimes;
	}

	public int getEMailReadDelay() {
		return emailReadDelay;
	}

	public String getMailFormat() {
		return mailFormat;
	}

	public String getSmsFormat() {
		return smsFormat;
	}

	public int getSmsReadDelay() {
		return smsReadDelay;
	}

	public String getSpecialCharacters() {
		return specialCharacters;
	}

	public boolean isCutName() {
		return cutName;
	}

	public boolean isCutNameAfterSpecialCharacters() {
		return cutNameAfterSpecialCharacters;
	}

	public boolean isEMailReadSubject() {
		return emailReadSubject;
	}

	public boolean isReadNumber() {
		return readNumber;
	}

	public boolean isReadUnknown() {
		return readUnknown;
	}

	public boolean isSmsRead() {
		return smsRead;
	}

	public boolean isStartSayCaller() {
		return startSayCaller;
	}

	public boolean isStartSayEMail() {
		return startSayEMail;
	}

	public boolean isStartSaySMS() {
		return startSaySMS;
	}

	public boolean isStartSomething() {
		return startSomething;
	}
}