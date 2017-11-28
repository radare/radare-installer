/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
(c) 2015-2017 pancake <pancake[at]nopcode[dot]org>
*/
package org.radare.radare2installer;

import org.radare.radare2installer.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.net.Uri;

public class LauncherActivity extends Activity {

	private Utils mUtils;

	private static String filterSingleQuote(String str) {
		if (str == null) {
			return null;
		}
		return str.replaceAll("\\\\", "").replaceAll("'", "");
	}

	private String findTerminalApp() {
		String[] apps = {
			"yarolegovich.materialterminal",
			"jackpal.androidterm"
		};
		for (int i = 0; i < apps.length; i++) {
			if (mUtils.isAppInstalled(apps[i])) {
				return apps[i];
			}
		}
		return null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// we don't need a layout for this activity as we finish() it right after the intent has started
		//setContentView(R.layout.launcher);

		mUtils = new Utils(getApplicationContext());

		Bundle b = getIntent().getExtras();
		String file_to_open = filterSingleQuote(b.getString("filename"));

		String term = findTerminalApp();
		if (term != null) {
			try {
				Intent i = new Intent(term + ".RUN_SCRIPT");
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.putExtra(term + ".iInitialCommand",
					  "export PATH=$PATH:/data/data/" + mUtils.PKGNAME + "/radare2/bin/"
					+ "; radare2 " + file_to_open + " || sleep 3"
					+ "; exit");
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			} catch (Exception e) {
				mUtils.myToast(e.toString(), Toast.LENGTH_LONG);
				mUtils.myToast("ERROR: Not enough permissions.\n"+
					"Please reinstall this application and try again.", Toast.LENGTH_LONG);
			}
		} else {
			mUtils.myToast("Please install Android Terminal Emulator and reinstall the radare2 app!\n\n"
				+ "(yarolegovich.materialterminal, jackpal.androidterm)", Toast.LENGTH_LONG);
			try {
				Intent i = new Intent(Intent.ACTION_VIEW); 
				i.setData(Uri.parse("market://details?id=jackpal.androidterm")); 
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPause(){
		finish();
		super.onPause();
	}
}
