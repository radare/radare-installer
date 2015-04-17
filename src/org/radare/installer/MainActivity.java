/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
    2015 Sergi Alvarez <pancake@nopcode.org>
*/
package org.radare.installer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.radare.installer.Utils;
import com.ice.tar.*;
import com.stericson.RootTools.*;

public class MainActivity extends Activity {
	
	private TextView outputView;
	private Handler handler = new Handler();
	private Button remoteRunButton;
	private Button localRunButton;
	
	private Context context;
	private Utils mUtils;
	/* TODO: use https, verify, signify */
	private String http_url_default = "http://radare.org/get/pkg/android/";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mUtils = new Utils(getApplicationContext());

		CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
		CheckBox checkHg = (CheckBox) findViewById(R.id.checkhg);

		String root = mUtils.GetPref("root");
		if (root.equals("yes")) checkBox.setChecked(true);
		else checkBox.setChecked(false);

		String version = mUtils.GetPref("version");
		if (version.equals("unstable")) checkHg.setChecked(true);
		if (version.equals("stable")) checkHg.setChecked(false);

		outputView = (TextView)findViewById(R.id.outputView);
		remoteRunButton = (Button)findViewById(R.id.remoteRunButton);
		remoteRunButton.setOnClickListener(onRemoteRunButtonClick);

		localRunButton = (Button)findViewById(R.id.localRunButton);
		localRunButton.setOnClickListener(onLocalRunButtonClick);

		output ("Welcome to radare2 installer!\nMake your selections on the checkbox above and click the INSTALL button to begin.\nYou can access more settings by pressing the menu button.\n\n");

		if (mUtils.isInternetAvailable()) {
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Thread thread = new Thread(new Runnable() {
				public void run() {
					String version = mUtils.GetPref("version");
					String ETag = mUtils.GetPref("ETag");
					RootTools.useRoot = false;
					if (!version.equals("unknown") && !ETag.equals("unknown") && RootTools.exists("/data/data/org.radare.installer/radare2/bin/radare2")) {
						output ("radare2 " + version + " is installed.\n");
						String arch = mUtils.GetArch();
						String http_url = prefs.getString ("http_url", http_url_default);
						String url = http_url + "/" + arch + "/" + version;
						boolean update = mUtils.UpdateCheck(url);
						if (update) {
							output ("New radare2 " + version + " version available!\nClick INSTALL to update now.\n");
							//mUtils.SendNotification("Radare2 update", "New radare2 " + version + " version available!\n");
						}
					}
				}
			});
			thread.start();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 0, "Settings");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
		}
		return false;
	}



	private OnClickListener onLocalRunButtonClick = new OnClickListener() {
		public void onClick(View v) {
			Thread thread = new Thread(new Runnable() {
				public void run() {

					localRunButton.setClickable(false);
					
					Intent intent = new Intent(MainActivity.this, LaunchActivity.class);
					startActivity(intent);      
					//finish();

					localRunButton.setClickable(true);
				}
			});
			thread.start();
		}
	};

	private OnClickListener onRemoteRunButtonClick = new OnClickListener() {
		public void onClick(View v) {

			//RootTools.debugMode = true;

			// disable button click if it has been clicked once
			remoteRunButton.setClickable(false);
			localRunButton.setClickable(false);
			//outputView.setText("");
			output ("");

			final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
			final CheckBox checkHg = (CheckBox) findViewById(R.id.checkhg);

			Thread thread = new Thread(new Runnable() {
				public void run() {

					String url;
					String hg;
					String output;
					String arch = mUtils.GetArch();
					String cpuabi = Build.CPU_ABI;

					output ("Detected CPU: " + cpuabi + " (" + arch +")\n");

					if (checkHg.isChecked()) {
						output("Download: unstable/development version\n");
						hg = "unstable";
					} else {
						output("Download: stable version\n");
						hg = "stable";
					}

					// store installed version in preferences
					mUtils.StorePref("version", hg);

					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					String http_url = prefs.getString ("http_url", http_url_default);
					url = http_url + "/" + arch + "/" + hg;

					/* fix broken stable URL in radare2 0.9 */
					/*
					if (cpuabi.matches(".*arm.*")) {
						boolean update = mUtils.UpdateCheck(url);
						if (!update) {
							if (!checkHg.isChecked()) url = "http://x90.es/radare2tar";
							else url = "http://x90.es/radare2git"; //for my tests
						}
					} */

					RootTools.useRoot = false;
					// remove old traces of previous r2 install
					mUtils.exec("rm -rf /data/data/org.radare.installer/radare2/");
					mUtils.exec("rm -rf /data/rata/org.radare.installer/files/");
					mUtils.exec("rm /data/data/org.radare.installer/radare2-android.tar");
					mUtils.exec("rm /data/data/org.radare.installer/radare2-android.tar.gz");

					boolean use_sdcard = prefs.getBoolean("use_sdcard", false);

					String storagePath = mUtils.GetStoragePath();

					long space = 0;
					long minSpace = 15;

					space = (mUtils.getFreeSpace("/data") / (1024*1024));
					output("Free space on data partition: " + space + "MB\n");

					if (space <= 0) {
						output("Warning: could not check space in data partition\n");
					} else if (space < minSpace) {
						output("Warning: low space on data partition\n");
						if (!use_sdcard) output ("If install fails, try to enable external storage in settings.\n");
					}

					if (use_sdcard) {
						mUtils.exec("rm -rf " + storagePath);
						//output("StoragePath = " + storagePath + "\n");
						space = (mUtils.getFreeSpace(storagePath.replace("/org.radare.installer/","")) / (1024*1024));
						output("Free space on external storage: " + space + "MB\n");
						if (space < minSpace) {	
							output("Warning: low space on external storage\n");
						}
					}

					String localPath = storagePath + "/radare2/tmp/radare2-android.tar.gz";

					// better than shell mkdir
					File dir = new File (storagePath + "/radare2/tmp");
					dir.mkdirs();
					boolean storageWriteable = dir.isDirectory();
					if (!storageWriteable) {
						output("ERROR: could not write to storage!\n");
					} else {
						output("Downloading radare2-android... please wait\n");
					}

					if (mUtils.isInternetAvailable() == false) {
						output("\nCan't connect to download server. Check that internet connection is available.\n");
					} else {

						RootTools.useRoot = false;
						// remove old traces of previous r2 download
						mUtils.exec("rm " + storagePath + "/radare2/tmp/radare2-android.tar");
						mUtils.exec("rm " + storagePath + "/radare2/tmp/radare2-android.tar.gz");

						// real download
						boolean downloadFinished = download(url, localPath);
						if (!downloadFinished) {
							output("ERROR: download could not complete\n");
						} else {
							output("Installing radare2... please wait\n");
						}

						try {
							unTarGz(localPath, storagePath + "/radare2/tmp/");
						} catch (Exception e) {
							e.printStackTrace();
						}

						// make sure we delete temporary files
						mUtils.exec("rm " + storagePath + "/radare2/tmp/radare2-android.tar");
						mUtils.exec("rm " + storagePath + "/radare2/tmp/radare2-android.tar.gz");

						// make sure bin files are executable
						mUtils.exec("chmod 755 /data/data/org.radare.installer/radare2/bin/*");
						mUtils.exec("chmod 755 /data/data/org.radare.installer/radare2/bin/");
						mUtils.exec("chmod 755 /data/data/org.radare.installer/radare2/");

						// make sure lib files are readable by other apps (for webserver using -c=h)
						mUtils.exec("chmod -R 755 /data/data/org.radare.installer/radare2/lib/");

						// setup temp folder for r2
						mUtils.exec("rm -rf /data/data/org.radare.installer/radare2/tmp/*");
						mUtils.exec("rm -rf /data/data/org.radare.installer/radare2/tmp");
						dir.mkdirs(); // better than shell mkdir
						mUtils.exec("chmod 1777 " + storagePath + "/radare2/tmp/");
						if (use_sdcard) {
							mUtils.exec ("ln -s " + storagePath + "/radare2/tmp /data/data/org.radare.installer/radare2/tmp");
						}

						boolean symlinksCreated = false;
						if (checkBox.isChecked()) {

							boolean isRooted = false;
							isRooted = RootTools.isAccessGiven();

							if(!isRooted) {
								output("\nCould not create xbin symlinks, got root?\n");
								mUtils.StorePref("root","no");
							} else { // device is rooted

								mUtils.StorePref("root","yes");

								RootTools.useRoot = true;

								output("\nCreating xbin symlinks...\n");
								RootTools.remount("/system", "rw");
								// remove old path
								mUtils.exec("rm -rf /data/local/radare2");
								// remove old symlinks in case they exist in old location
								mUtils.exec("rm -rf /system/xbin/radare2 /system/xbin/r2 /system/xbin/rabin2 /system/xbin/radiff2 /system/xbin/ragg2 /system/xbin/rahash2 /system/xbin/ranal2 /system/xbin/rarun2 /system/xbin/rasm2 /system/xbin/rax2 /system/xbin/rafind2 /system/xbin/ragg2-cc");

								if (RootTools.exists("/data/data/org.radare.installer/radare2/bin/radare2")) {

									// show output for the first link, in case there's any error with su
									output = mUtils.exec("ln -s /data/data/org.radare.installer/radare2/bin/radare2 /system/xbin/radare2 2>&1");
									if (!output.equals("")) output(output);

									String file;
									File folder = new File("/data/data/org.radare.installer/radare2/bin/");
									File[] listOfFiles = folder.listFiles(); 
									for (int i = 0; i < listOfFiles.length; i++) {
										if (listOfFiles[i].isFile()) {
											file = listOfFiles[i].getName();
											mUtils.exec("ln -s /data/data/org.radare.installer/radare2/bin/" + file + " /system/xbin/" + file);
											output("linking /system/xbin/" + file + "\n");
										}
									}
								}

								RootTools.remount("/system", "ro");
								if (RootTools.exists("/system/xbin/radare2")) {
									output("done\n");
									symlinksCreated = true;
								} else {
									output("\nFailed to create xbin symlinks\n");
									symlinksCreated = false;
								}

								RootTools.useRoot = false;
							}
						}

						RootTools.useRoot = false;
						if (!RootTools.exists("/data/data/org.radare.installer/radare2/bin/radare2")) {
							localRunButton.setClickable(false);
							output("\n\nsomething went wrong during installation :(\n");
						} else {
							localRunButton.setClickable(true);
							//if (!symlinksCreated) output("\nRadare2 is installed in:\n   /data/data/org.radare.installer/radare2/\n");
							output("\nTesting installation:\n\n$ radare2 -v\n");
							output = mUtils.exec("/data/data/org.radare.installer/radare2/bin/radare2 -v");
							if (!output.equals("")) output(output);
							else output("Radare was not installed successfully, make sure you have enough space in /data and try again.");
						}
					}
					// enable button again
					remoteRunButton.setClickable(true);
					localRunButton.setClickable(true);
				}
			});
			thread.start();
		}
	};



	private void output(final String str) {
		Runnable proc = new Runnable() {
			public void run() {
				if (str!=null) outputView.append(str);
				if (str.equals("")) outputView.setText("");
			}
		};
		handler.post(proc);
	}


	public static void unTarGz(final String zipPath, final String unZipPath) throws Exception {
		GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(zipPath));

		//path and name of the tempoary tar file
		String tempDir = unZipPath.substring(0, unZipPath.lastIndexOf('/'));
		String tempFile = "radare-android.tar";
		String tempPath = tempDir + "/" + tempFile;

		//first we create the gunzipped tarball...
		OutputStream out = new FileOutputStream(tempPath);

		byte[] data = new byte[1024];
		int len;
		while ((len = gzipInputStream.read(data)) > 0) {
			out.write(data, 0, len);
		}

		gzipInputStream.close();
		out.close();

		//...then we use com.ice.tar to extract the tarball contents
		TarArchive tarArchive = new TarArchive(new FileInputStream(tempPath));
		tarArchive.extractContents(new File("/"));
		tarArchive.closeArchive();

		//remove the temporary gunzipped tar
		new File(tempPath).delete();
	}

	private boolean download(String urlStr, String localPath) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection urlconn = (HttpURLConnection)url.openConnection();
			urlconn.setRequestMethod("GET");
			urlconn.setInstanceFollowRedirects(true);
			urlconn.getRequestProperties();
			urlconn.connect();
			String mETag = urlconn.getHeaderField("ETag");
			mUtils.StorePref("ETag",mETag);
			InputStream in = urlconn.getInputStream();
			FileOutputStream out = new FileOutputStream(localPath);
			int read;
			byte[] buffer = new byte[4096];
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			out.close();
			in.close();
			urlconn.disconnect();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void onResume() {
		// if updates are enabled, make sure the alarm is set...
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String hoursStr = prefs.getString("updates_interval", "12");
		int hours = Integer.parseInt(hoursStr);
		boolean perform_updates = prefs.getBoolean("perform_updates", true);
		if (perform_updates) {
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			Intent i = new Intent(this, UpdateCheckerService.class);
			PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
			am.cancel(pi);
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + hours*60*60*1000, hours*60*60*1000, pi);
		}
	}
}
