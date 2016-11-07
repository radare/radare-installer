/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
    2015 pancake <pancake[at]nopcode[dot]org>
*/
package org.radare.radare2installer;

import org.radare.radare2installer.Utils;
import org.radare.r2pipe.R2Pipe;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

import android.view.KeyEvent;
import android.net.Uri;

import android.widget.Toast;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Enumeration;

import com.stericson.RootTools.*;

import android.util.Log;
//import android.webkit.ConsoleSettings;
// import android.webkit.ConsoleChromeClient;

public class ConsoleActivity extends Activity {

	private static final String TAG = "radare2-ConsoleActivity";
	private Utils mUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUtils = new Utils(getApplicationContext());

		setContentView(R.layout.console);

		RootTools.useRoot = false;

		// get shell first
		try {
			RootTools.getShell(RootTools.useRoot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// make sure we don't start a second instance of radare webserver
		mUtils.killradare();

		Bundle b = getIntent().getExtras();
		String file_to_open = b.getString("filename");
		String input = b.getString("consoleInput");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUtils.killradare();
	}
/*
	@Override
	public void onStop() {
		super.onStop();
		Log.v(TAG, "onStop() called");
		mUtils.killradare();
	}
*/
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				Log.v(TAG, "onKeyDown() called");
				//webview.goBack();
				mUtils.killradare();
				//finish();
				return true;
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return super.onKeyDown(keyCode, event);
	}
}
