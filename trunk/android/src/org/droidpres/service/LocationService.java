/*******************************************************************************
 * Copyright (c) 2011 Eugene Vorobkalo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eugene Vorobkalo - initial API and implementation
 ******************************************************************************/
package org.droidpres.service;

import org.droidpres.activity.SetupRootActivity;
import org.droidpres.db.DBDroidPres;
import org.droidpres.receiver.OnAlarmReceiver;
import org.droidpres.utils.PositionManager;
import org.droidpres.utils.PositionManager.LocationHandler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

public class LocationService extends Service {
	private static final String TAG = "LOCATION_SERVICE";

	private static final int DELAY_FIRST_RUN	= 5 * 1000;
	private static final int DELAY_INTERVAL	= 15 * 60 * 1000;
	private static final int DELAY_WORK		=  4 * 60 * 1000;
	
	private static final int REQUEST_CODE = 100;

	private AlarmManager mAlarmManager;
	private PendingIntent mPendingIntent;	
	private static SQLiteDatabase mDataBase;
	private PositionManager mPositionManager;
	private int mAccuracy;
//	private WakeLock mWakeLock;
	
	@Override
	public void onCreate() {
		super.onCreate();

		mPendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE,
				new Intent(this, OnAlarmReceiver.class), 0);

		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + DELAY_FIRST_RUN, DELAY_INTERVAL,
				mPendingIntent);
		
		mAccuracy = SetupRootActivity.getGPSAccuracy(getBaseContext());
		mPositionManager = new PositionManager(this, mLocationHandler, mAccuracy);
//		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(TAG, "Service onStart(): " + 
				new java.sql.Timestamp(System.currentTimeMillis()).toString() + " StartId:" + startId);
		
		if (startId > 1) startLocation();
	}

	@Override
	public void onDestroy() {
//		mWakeLock.release();
		Log.i(TAG, "Service onDestroy(): " + 
				new java.sql.Timestamp(System.currentTimeMillis()).toString());

		stopLocation();
		if (mAlarmManager != null) 	mAlarmManager.cancel(mPendingIntent);
	}
	 
	@Override
	public IBinder onBind(Intent intent) {
	    Log.i(TAG, "onBind(..)");
	    return null;
	}
	
	private void startLocation() {
		Log.i(TAG, "startLocation()");
//		mWakeLock.acquire(DELAY_WORK);
		mDataBase = (new DBDroidPres(this)).getWritableDatabase();
		mPositionManager.resume();
		mTimerHandler.sendEmptyMessageDelayed(0, DELAY_WORK);
	}

	private void stopLocation() {
		Log.i(TAG, "stopLocation()");
		mPositionManager.pause();
		if (mDataBase != null) mDataBase.close();
//		mWakeLock.release();
	}
	
	private LocationHandler mLocationHandler = new LocationHandler() {
		public void onLocation(Location location) {
			String provider = location.getProvider();
			if (TextUtils.isEmpty(provider)) provider = "Unknown";
			final ContentValues cval = new ContentValues();
			cval.put("provider",	provider);
			cval.put("lat",	(int) (location.getLatitude() * 1E6));
			cval.put("lon",	(int) (location.getLongitude() * 1E6));
			cval.put("accuracy",	(int) location.getAccuracy());
			mDataBase.insertOrThrow(DBDroidPres.TABLE_LOCATION, null, cval);
			if (location.getAccuracy() <= mAccuracy) stopLocation();
		}
	};
	
	private final Handler mTimerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			stopLocation();
		}
	};
}