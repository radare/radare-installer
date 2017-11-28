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

import android.view.Window;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;

import java.util.Enumeration;

import com.stericson.RootTools.*;

import android.util.Log;
//import android.webkit.ConsoleSettings;
// import android.webkit.ConsoleChromeClient;

public class ConsoleActivity extends Activity {

	private static final String TAG = "radare2-ConsoleActivity";
	private Utils mUtils;
	private R2Pipe r2p;
	private Button QUIT;
	private Button CLEAR;
	private Button RUN;
	private EditText INPUT;
	private TextView OUTPUT;
	private ScrollView SCROLL;
	private Handler handler = new Handler();

	public void runInputCommand() {
		String input = INPUT.getText().toString();
		if (input.equals("")) {
			return;
		}
		try {
			output("> " + input + "\n" + r2p.cmd(input) + "\n");
		} catch (Exception e) {
			output("> " + input + "\n" + e.toString() + "\n");
		}
		SCROLL.scrollBy(0, OUTPUT.getText().length());
		SCROLL.fullScroll(ScrollView.FOCUS_DOWN);
		INPUT.requestFocus();
	}

	private OnClickListener onRun = new OnClickListener() {
		public void onClick(View v) {
			runInputCommand();
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
				/* scroll to bottom is done by gravity */
				// OUTPUT.scrollBy(0, 128);
				SCROLL.scrollBy(0, OUTPUT.getText().length());
				SCROLL.fullScroll(ScrollView.FOCUS_DOWN);
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

	private OnClickListener onClear = new OnClickListener() {
		public void onClick(View v) {
			INPUT.setText("");
			OUTPUT.setText("");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mUtils = new Utils(getApplicationContext());

		setContentView(R.layout.console);

		RootTools.useRoot = false;

		INPUT = (EditText)findViewById(R.id.consoleInput);
		INPUT.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					runInputCommand();
					return true;
				}
				return true;
			}
		});
		OUTPUT = (TextView)findViewById(R.id.consoleOutput);
		SCROLL = (ScrollView)findViewById(R.id.scrollOutput);
		SCROLL.fullScroll(ScrollView.FOCUS_DOWN);

		RUN = (Button)findViewById(R.id.runButton);
		RUN.setOnClickListener(onRun);

		QUIT = (Button)findViewById(R.id.quitButton);
		QUIT.setOnClickListener(onQuit);
		CLEAR = (Button)findViewById(R.id.clearButton);
		CLEAR.setOnClickListener(onClear);

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
