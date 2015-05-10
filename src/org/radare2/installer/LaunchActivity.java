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

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.net.Uri;

import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

import android.widget.Toast;
import java.io.File;

public class LaunchActivity extends Activity {

	private Utils u;

	private RadioGroup radiogroup;
	private Button btnDisplay;
	private Button btnDebug;
	private EditText file_to_open;

	private boolean checkForRadare() {
		File radarebin = new File("/data/data/org.radare2.installer/radare2/bin/radare2");
		boolean ex = radarebin.exists();
		if (!ex) {
			Intent i = new Intent(LaunchActivity.this, MainActivity.class);
	//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			u.myToast("Please install radare2 first!", Toast.LENGTH_SHORT);
		}
		return ex;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		u = new Utils(getApplicationContext());

		checkForRadare();
		setContentView(R.layout.launch);
		// Get intent, action and extras
		Intent intent = getIntent();
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();

		setContentView(R.layout.launch);

		radiogroup = (RadioGroup) findViewById(R.id.radiogroup1);
		String open_mode = u.GetPref("open_mode");
		if (open_mode.equals("web")) {
			radiogroup.check(R.id.radiobutton_web);
		}
		if (open_mode.equals("browser")) {
			radiogroup.check(R.id.radiobutton_browser);
		}
		if (open_mode.equals("console")) {
			radiogroup.check(R.id.radiobutton_console);
		}

		String path = u.GetPref("last_opened");
		if (path.equals("unknown")) path = "/system/bin/toolbox";
		if (Intent.ACTION_SEND.equals(action)) {
			Uri uri = (Uri)bundle.get(Intent.EXTRA_STREAM);
			path = uri.decode(uri.toString());
			if (path.startsWith("file://")) {
				path = path.replace("file://", "");
			}
			if (path.startsWith("content://")) {
				path = path.replaceAll("content://[^/]*", "");
			}
			if (path.endsWith(".apk") || path.endsWith(".APK")) {
				path = path.replaceAll("^", "apk://");
			}
			if (path == null) path = "/system/bin/toolbox";
		} 
		file_to_open = (EditText) findViewById(R.id.file_to_open);
		file_to_open.setText(path, TextView.BufferType.EDITABLE);
		addListenerOnButton();
	}

	@Override
	public void onDestroy() {
		// killing r2 here destroys the webview/browser views
		//u.killRadare();
		super.onDestroy();
	}

	public void startStuff(String arg) {
		int selectedId = radiogroup.getCheckedRadioButtonId();

		if (!checkForRadare())
			return;

		file_to_open = (EditText) findViewById(R.id.file_to_open);
		Bundle b = new Bundle();
		b.putString("filename", arg+"\"" + file_to_open.getText().toString() + '"');
		u.StorePref("last_opened",file_to_open.getText().toString());

		switch (selectedId) {
		case R.id.radiobutton_web :
			u.StorePref("open_mode","web");
			Intent intent1 = new Intent(LaunchActivity.this, WebActivity.class);
			b.putString("mode", "web");
			intent1.putExtras(b);
			startActivity(intent1);
			break;
		case R.id.radiobutton_browser :
			u.StorePref("open_mode","browser");
			Intent intent2 = new Intent(LaunchActivity.this, WebActivity.class);
			b.putString("mode", "browser");
			intent2.putExtras(b);
			startActivity(intent2);
			break;
		case R.id.radiobutton_console :
			u.StorePref("open_mode","console");
			Intent intent3 = new Intent(LaunchActivity.this, LauncherActivity.class);
			b.putString("mode", "console");
			intent3.putExtras(b);
			startActivity(intent3);
			break;
		}
	}

	public void addListenerOnButton() {
		radiogroup = (RadioGroup) findViewById(R.id.radiogroup1);
		btnDebug = (Button) findViewById(R.id.button_debug);
		btnDebug.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startStuff ("-d ");
			}
		});

		btnDisplay = (Button) findViewById(R.id.button_open);
		btnDisplay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startStuff ("");
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, Menu.NONE, "Settings");
		menu.add(Menu.NONE, 1, Menu.NONE, "Installer");
		menu.add(Menu.NONE, 2, Menu.NONE, "About");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			startActivity(new Intent(LaunchActivity.this, SettingsActivity.class));
			return true;
		case 1:
			startActivity(new Intent(LaunchActivity.this, MainActivity.class));
			return true;
		case 2:
			try {
				if (u == null) {
					u = new Utils(getApplicationContext());
				}
				u.myToast("authors: pof & pancake", Toast.LENGTH_SHORT);
			} catch (Exception e) {
				// err
			}
			return true;
		}
		return false;
	}
}
