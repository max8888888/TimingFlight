package com.pb.app.timingflight.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.format.Time;

/**
 * Data structure for holding AutoFlighty configurations.
 * Should always be consistent with local config file as SharedPreferences.
 */
public class Config {
	
	// Keys to configurations in SharedPreference files.
	private static final String KEY_ON_ENABLED = "turn_on_enabled";
	private static final String KEY_OFF_ENABLED = "turn_off_enabled";
	private static final String KEY_ON_HOUR = "turn_on_hour";
	private static final String KEY_ON_MINUTE = "turn_on_minute";
	private static final String KEY_OFF_HOUR = "turn_off_hour";
	private static final String KEY_OFF_MINUTE = "turn_off_minute";
	
	private static final long DAY = 24*60*60*1000;
	
	// Local config file as SharedPreferences.
	private SharedPreferences mPrefs;
	private Editor mEditor;
	
	public Config(Context context) {
		mPrefs = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		mEditor = mPrefs.edit();
	}
	
	public boolean isTurnOnAirplaneModeEnabled() {
		return mPrefs.getBoolean(KEY_ON_ENABLED, false);
	}
	public boolean isTurnOffAirplaneModeEnabled() {
		return mPrefs.getBoolean(KEY_OFF_ENABLED, false);
	}
	
	public Time getTurnOnTime() {
		Time time = new Time();
		time.set(0, mPrefs.getInt(KEY_ON_MINUTE, 0), mPrefs.getInt(KEY_ON_HOUR, 0),
				0, 0, 0);
		return time;
	}
    public Time getTurnOffTime() {
		Time time = new Time();
		time.set(0, mPrefs.getInt(KEY_OFF_MINUTE, 0), mPrefs.getInt(KEY_OFF_HOUR, 0),
				0, 0, 0);
		return time;
	}
	
	/**
	 * @return The next auto-on time calculated from config. This method does not
	 * check validation, but assumes auto-on is enabled with valid timing.
	 */
	long getNextTurnOnTime() {
		Time time = new Time();
		time.setToNow();
		time.minute = mPrefs.getInt(KEY_ON_MINUTE, 0);
		time.hour = mPrefs.getInt(KEY_ON_HOUR, 0);
		long ans = time.normalize(true);
		return ans > System.currentTimeMillis() ? ans : ans + DAY;
	}
	/**
	 * @return The next auto-off time calculated from config. This method does not
	 * check validation, but assumes auto-on is enabled with valid timing.
	 */
	long getNextTurnOffTime() {
		Time time = new Time();
		time.setToNow();
		time.minute = mPrefs.getInt(KEY_OFF_MINUTE, 0);
		time.hour = mPrefs.getInt(KEY_OFF_HOUR, 7);
		long ans = time.normalize(true);
		return ans > System.currentTimeMillis() ? ans : ans + DAY;
	}
	
	public void setTurnOnAirplaneModeEnabled(boolean onEnabled) {
		mEditor.putBoolean(KEY_ON_ENABLED, onEnabled).commit();
	}
	public void setTurnOffAirplaneModeEnabled(boolean offEnabled) {
		mEditor.putBoolean(KEY_OFF_ENABLED, offEnabled).commit();
	}
	
	public void setTurnOnHourMinute(int hour, int minute) {
		mEditor.putInt(KEY_ON_HOUR, hour).putInt(KEY_ON_MINUTE, minute).commit();
	}
	public void setTurnOffHourMinute(int hour, int minute) {
		mEditor.putInt(KEY_OFF_HOUR, hour).putInt(KEY_OFF_MINUTE, minute).commit();
	}
	
	public void registerOnAlarm(Context context, AlarmManager alarmManager) {

		Intent intent = new Intent(FlightIntentReceiver.TURN_ON_AIRPLANE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		alarmManager.setRepeating(AlarmManager.RTC, getNextTurnOnTime(), DAY, pendingIntent);
	}
	
	public void cancelOnAlarm(Context context, AlarmManager alarmManager) {

		Intent intent = new Intent(FlightIntentReceiver.TURN_ON_AIRPLANE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		alarmManager.cancel(pendingIntent);
	}
	
	public void registerOffAlarm(Context context, AlarmManager alarmManager) {
		
		Intent intent = new Intent(FlightIntentReceiver.TURN_OFF_AIRPLANE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		alarmManager.setRepeating(AlarmManager.RTC, getNextTurnOffTime(), DAY, pendingIntent);
	}
	
	public void cancelOffAlarm(Context context, AlarmManager alarmManager) {

		Intent intent = new Intent(FlightIntentReceiver.TURN_OFF_AIRPLANE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		alarmManager.cancel(pendingIntent);
	}
}