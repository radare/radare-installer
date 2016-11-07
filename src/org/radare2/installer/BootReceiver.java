/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
(c) 2015-2016 pancake <pancake[at]nopcode[dot]org>
*/
package org.radare2.installer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;

public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String hoursStr = prefs.getString("updates_interval", "12");
		int hours = Integer.parseInt(hoursStr);
		boolean perform_updates = prefs.getBoolean("perform_updates", true);
		if (perform_updates) {
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, UpdateCheckerService.class);
			PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
			am.cancel(pi);
			am.setInexactRepeating(
				AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime()
					+ hours*60*60*1000,
				hours*60*60*1000, pi);
		}
	}
}
