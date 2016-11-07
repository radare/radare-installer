/*
radare2 installer for Android
(c) 2012-2013 Pau Oliva Fora <pof[at]eslack[dot]org>
    2015 pancake <pancake[at]nopcode[dot]org>
*/
package org.radare.radare2installer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.os.Bundle;

public class PathReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {

		String packageName = context.getPackageName();
		String action = intent.getAction();

		/**
		* You need to declare the permission
		* jackpal.androidterm.permission.APPEND_TO_PATH
		* to receive this broadcast.
		*/
		if (action.equals("jackpal.androidterm.broadcast.APPEND_TO_PATH")) {
			/* The directory we want appended goes into the result extras */
			Bundle result = getResultExtras(true);
			result.putString(packageName, "/data/data/org.radare.radare2installer/radare2/bin/");
			setResultCode(Activity.RESULT_OK);
		}
	}
}
