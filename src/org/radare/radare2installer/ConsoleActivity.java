/*
radare2 installer for Android
(c) 2016 pancake <pancake[at]nopcode[dot]org>
*/
package org.radare.radare2installer;

import org.radare.radare2installer.Utils;
import org.radare.r2pipe.R2Pipe;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.widget.*;

import android.view.KeyEvent;
import android.net.Uri;

import android.widget.Toast;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.Enumeration;

import com.stericson.RootTools.*;

import android.util.Log;
//import android.webkit.ConsoleSettings;
// import android.webkit.ConsoleChromeClient;

public class ConsoleActivity extends Activity {

	private static final String TAG = "radare2-ConsoleActivity";
	private Utils mUtils;
	private R2Pipe r2p;
	private Button RUN;
	private Button QUIT;
	private EditText INPUT;
	private TextView OUTPUT;
	private ScrollView SCROLL;
	private Handler handler = new Handler();

	private OnClickListener onRun = new OnClickListener() {
		public void onClick(View v) {
			String input = INPUT.getText().toString();
			try {
				output("> " + input + "\n" + r2p.cmd(input) + "\n");
			} catch (Exception e) {
				output("> " + input + "\n" + e.toString() + "\n");
			}
		}
	};

	private void output(final String str) {
		Runnable proc = new Runnable() {
			public void run() {
				if (str != null) {
					OUTPUT.append(str);
				} else {
					OUTPUT.setText("");
				}
				INPUT.setText("");
				/* scroll to bottom */
				OUTPUT.scrollBy(0, 128);
				SCROLL.scrollBy(0, 128);
				//final int scrollAmount = OUTPUT.getLayout().getLineTop(OUTPUT.getLineCount()) - OUTPUT.getHeight();
				//OUTPUT.scrollTo(0, Math.max(scrollAmount, 0));
				//SCROLL.fullScroll(View.FOCUS_DOWN);
			}
		};
		handler.post(proc);
	}

	private OnClickListener onQuit = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUtils = new Utils(getApplicationContext());

		setContentView(R.layout.console);

		RootTools.useRoot = false;

		INPUT = (EditText)findViewById(R.id.consoleInput);
		OUTPUT = (TextView)findViewById(R.id.consoleOutput);
		OUTPUT = (TextView)findViewById(R.id.consoleOutput);
		SCROLL = (ScrollView)findViewById(R.id.scrollOutput);
		// OUTPUT.setMovementMethod(new ScrollingMovementMethod());
		RUN = (Button)findViewById(R.id.runButton);
		RUN.setOnClickListener(onRun);

		QUIT = (Button)findViewById(R.id.quitButton);
		QUIT.setOnClickListener(onQuit);

		// get shell first
		try {
			RootTools.getShell(RootTools.useRoot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// make sure we don't start a second instance of radare webserver
		mUtils.killradare();

		Bundle b = getIntent().getExtras();
		String filename = b.getString("filename").replaceAll("\"", "").replaceAll("\n",";");
		output("$ r2 " + filename + "\n");
		try {
			if (filename.startsWith("http://")) {
				r2p = new R2Pipe(filename, "", true);
			} else {
				r2p = new R2Pipe(filename, "/data/data/" + mUtils.PKGNAME + "/radare2/bin/radare2", false);
			}
		} catch (Exception e) {
			mUtils.myToast(e.toString(), Toast.LENGTH_SHORT);
		}
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
		//		mUtils.killradare();
				//finish();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.onKeyDown(keyCode, event);
	}
}
