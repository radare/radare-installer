/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
(c) 2015 pancake <pancake[at]nopcode[dot]org>
*/
package org.radare2.installer;

import org.radare2.installer.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.net.Uri;

public class LauncherActivity extends Activity {

	private Utils mUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// we don't need a layout for this activity as we finish() it right after the intent has started
		//setContentView(R.layout.launcher);

		mUtils = new Utils(getApplicationContext());

		Bundle b = getIntent().getExtras();
		String file_to_open = b.getString("filename");


		if (mUtils.isAppInstalled("jackpal.androidterm")) {
			try {
				Intent i = new Intent("jackpal.androidterm.RUN_SCRIPT");
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.putExtra("jackpal.androidterm.iInitialCommand", "export PATH=$PATH:/data/data/org.radare2.installer/radare2/bin/ ; radare2 " + file_to_open + " ; exit");
				startActivity(i);
			} catch (Exception e) {
				mUtils.myToast("ERROR: Not enough permissions.\nPlease reinstall this application and try again.", Toast.LENGTH_LONG);
			}
		} else {
			//Toast.makeText(getApplicationContext(), "terminal not installed", Toast.LENGTH_LONG).show();
			mUtils.myToast("Please install Android Terminal Emulator first!", Toast.LENGTH_LONG);
			try {
				Intent i = new Intent(Intent.ACTION_VIEW); 
				i.setData(Uri.parse("market://details?id=jackpal.androidterm")); 
				startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		finish();
	}

}
