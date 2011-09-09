package com.proch.practicetools;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MetronomeService extends Service {

	private final IBinder mBinder = new MetronomeBinder();
	private PowerManager.WakeLock mWakeLock;
	private Metronome mMetronome;
	private static final int ONGOING_NOTIFICATION = 1337;
	private static MetronomeService instance = null;

	@Override
	public void onCreate() {
		Log.i("", "Creating service");
		instance = this;
		mMetronome = new Metronome(getApplicationContext());

		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MetronomeLock");
	}

	@Override
	public void onDestroy() {
		stopMetronome();
		mMetronome.close();
		instance = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("", "Calling onBind");
		return mBinder;
	}

	public static boolean isRunning() {
		return (instance != null && instance.mMetronome.isRunning());
	}

	public void startMetronome(int tempo, int beatsOn, int beatsOff) {
		startNotification();
		mWakeLock.acquire();
		mMetronome.start(tempo, beatsOn, beatsOff);
	}

	public void stopMetronome() {
		mMetronome.stop();
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
		stopNotification();
	}

	public void restartMetronome(int tempo, int beatsOn, int beatsOff) {
		mMetronome.restart(tempo, beatsOn, beatsOff);
	}

	public class MetronomeBinder extends Binder {
		MetronomeService getService() {
			return MetronomeService.this;
		}
	}

	private void startNotification() {
		Notification notification = new Notification(R.drawable.ic_stat_metronome, "", System
				.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainScreen.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(this, getString(R.string.app_name), "Metronome running...",
				pendingIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		startForeground(ONGOING_NOTIFICATION, notification);
	}

	private void stopNotification() {
		stopForeground(true);
	}
}
