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

	private int emailSubjectReadDelay;
	private boolean emailCutReFwd;
	private boolean emailReadSubject;
	private int emailRepeatTimes;
	private boolean emailReadSubjectDiscreet;

	private boolean startSomething;
	private boolean startSayCaller;
	private boolean startSaySMS;
	private boolean startSayEMail;

	private String callerFormatString;
	private String smsFormatString;
	private String emailFormatString;

	private int wantedVolume;

	private boolean cutName;
	private boolean cutNameAfterSpecialCharacters;
	private String specialCharacters = ":/-(";
	private boolean readNumber;

	private boolean discreetMode;


	public Settings(Context context) {
		// read and save a lot of settings
		SharedPreferences preferences = context.getSharedPreferences("saysomething", Context.MODE_WORLD_WRITEABLE);

		startSayCaller = preferences.getBoolean("saycaller", true);
		startSaySMS = preferences.getBoolean("saysms", true);
		startSayEMail = preferences.getBoolean("sayemail", true);
		startSomething = preferences.getBoolean("saysomething", true);

		if (!startSayCaller && !startSaySMS && !startSayEMail) {
			startSomething = false;
		}

		callerRepeatSeconds = Integer.parseInt(preferences.getString("callerRepeatSeconds", "3"));

		callerRepeatTimes = Integer.parseInt(preferences.getString("callerRepeatTimes", "4"));

		callerFormatString = preferences.getString("callerFormat", "");
		smsFormatString = preferences.getString("smsFormat", "");
		emailFormatString = preferences.getString("emailFormat", "");

		wantedVolume = Integer.parseInt(preferences.getString("wantedVolume", "5"));

		cutName = preferences.getBoolean("cutName", true);
		cutNameAfterSpecialCharacters = preferences.getBoolean("cutNameAfterSpecialCharacters", false);
		specialCharacters =	preferences.getString("specialCharacters", ":/-(");
		readNumber = preferences.getBoolean("readNumber", false);

		discreetMode = preferences.getBoolean("discreetMode", false);

		smsRead = preferences.getBoolean("smsRead", false);
		smsReadDelay = Integer.parseInt(preferences.getString("smsReadDelay", "3"));
		smsRepeatTimes = Integer.parseInt(preferences.getString("smsRepeatTimes", "1"));
		smsReadDiscreet = preferences.getBoolean("smsReadDiscreet", true);
		
		emailReadSubject = preferences.getBoolean("emailReadSubject", false);
		emailSubjectReadDelay = Integer.parseInt(preferences.getString("emailReadDelay", "2"));
		emailCutReFwd = preferences.getBoolean("smsReadDiscreet", false);		
		emailRepeatTimes = Integer.parseInt(preferences.getString("emailRepeatTimes", "1"));
		emailReadSubjectDiscreet = preferences.getBoolean("emailReadSubjectDiscreet", true);
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
	
	public boolean isStartSayEMail() {
		return startSayEMail;
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
	
	public String getEMailFormatString() {
		return emailFormatString;
	}

	public int getWantedVolume() {
		return wantedVolume;
	}

	public boolean isCutName() {
		return cutName;
	}

	public boolean isCutNameAfterSpecialCharacters(){
		return cutNameAfterSpecialCharacters;
	}

	public String getSpecialCharacters(){
		return specialCharacters;
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

	public boolean isEMailCutReFwd(){
		return emailCutReFwd;
	}
	
	public boolean isEMailReadSubject(){
		return emailReadSubject;
	}
	
	public boolean isEMailReadSubjectDiscreet(){
		return emailReadSubjectDiscreet;
	}
	
	public int getEMailRepeatTimesSubject(){
		return emailRepeatTimes;
	}
	
	public int getEMailReadSubjectDelay(){
		return emailSubjectReadDelay;
	}
	
	public int getSmsRepeatTimes() {
		return smsRepeatTimes;
	}

	public boolean isReadNumber() {
		return readNumber;
	}
}