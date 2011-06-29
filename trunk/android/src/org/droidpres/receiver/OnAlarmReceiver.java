package org.droidpres.receiver;

import java.util.Calendar;

import org.droidpres.service.LocationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnAlarmReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		Log.i("OnAlarmService", "onReceive: " + hour + "hour");
		context.startService(new Intent(context, LocationService.class));

		if (hour < 8 || hour > 19) return;
	}
}
