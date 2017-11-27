/*
radare2 installer for Android
(c) 2012      Pau Oliva Fora <pof[at]eslack[dot]org>
    2015-2017 pancake <pancake[at]nopcode[dot]org>
*/
package org.radare.radare2installer;

import org.radare.radare2installer.Utils;

import android.app.Activity;
import android.app.ActionBar;
import android.os.Bundle;
import android.content.Intent;

import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.net.Uri;

import android.widget.Toast;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.http.conn.util.InetAddressUtils;

import com.stericson.RootTools.*;

import android.util.Log;
//import android.webkit.WebSettings;
import android.webkit.WebChromeClient;

public class WebActivity extends Activity {

	private static final String TAG = "radare2-WebActivity";
	private Utils mUtils;

        WebView webview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUtils = new Utils(getApplicationContext());

		setContentView(R.layout.webactivity);

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

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String http_port = prefs.getString("http_string", "9090");
		boolean http_upload = prefs.getBoolean("http_upload", false);
		boolean http_public = prefs.getBoolean("http_public", false);
		boolean http_sandbox = prefs.getBoolean("http_sandbox", true);
		String r2args = " -e http.port=" + http_port;

		r2args += " -e http.sandbox=" + String.valueOf(http_sandbox);
		if (http_public) {
			r2args = " -e http.bind=public ";
			String localip = getLocalIpAddress();
			if (localip != null) {
				mUtils.myToast("r2 http server\n" + localip + ":" + http_port, Toast.LENGTH_LONG);
				Log.v(TAG, "ip address: " + localip);
			}
		}
		if (http_upload) {
			r2args += " -e http.upload=true ";
		}
		Log.v(TAG, "r2args: " + r2args);

		String output = mUtils.exec("/data/data/" + mUtils.PKGNAME + "/radare2/bin/radare2 " + 
			r2args + " -c=h " + file_to_open + " &");
		Log.v(TAG, "radare2 started");

		mUtils.sleep (1);

		if (true) { //RootTools.isProcessRunning("radare2")) {
			String open_mode = mUtils.GetPref("open_mode");
			if (open_mode.equals("browser")) {
				String url = "http://localhost:" + http_port + "/m";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.setData(Uri.parse(url));
				startActivity(i);
				Log.v(TAG, "Browser started");
			} else if (open_mode.equals("web")) {
				try {
					webview = (WebView) findViewById(R.id.webview);
					webview.setWebViewClient(new RadareWebViewClient());
					webview.setWebChromeClient(new WebChromeClient());
					webview.setWebContentsDebuggingEnabled(true);
					WebSettings ws = webview.getSettings();
					ws.setJavaScriptEnabled(true);
					ws.setBuiltInZoomControls(false);
					ws.setSupportZoom(true);
					ws.setUseWideViewPort(true);
					ws.setLoadWithOverviewMode(true);
					ws.setBlockNetworkImage(false);
					ws.setDomStorageEnabled(true);
					ws.setDatabaseEnabled(true);
/*
					// Fix for pre-kitkat devices
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
						ws.setDatabasePath("/data/data/" + mUtils.PKGNAME + "webstore");
					}
*/
					/* ... */
				} catch (Exception e) {
				}
				{
					final ActionBar actionBar = getActionBar();
					actionBar.hide();
					actionBar.setDisplayShowTitleEnabled(false);
				}
	/*
				webview.getSettings().setDomStorageEnabled(true);
				webview.getSettings().setSupportMultipleWindows(true);
				webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
				webview.getSettings().setAppCacheMaxSize(1024*1024*16);
				String appCachePath = "/data/data/" + getPackageName() + "/cache/";
				webview.getSettings().setAppCachePath(appCachePath);
				webview.getSettings().setAllowFileAccess(true);
				webview.getSettings().setAppCacheEnabled(true);
				webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
				webview.getSettings().setDatabaseEnabled(true);
				String databasePath = "/data/data/" + getPackageName() + "/databases/";
				webview.getSettings().setDatabasePath(databasePath);
				webview.getSettings().setGeolocationEnabled(true);
				webview.getSettings().setSaveFormData(true);

				webview.getSettings().setAllowContentAccess(true);
				//    webview.getSettings().setAllowFileAccessFromFileURLs(true);
				//    webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
				webview.getSettings().setBuiltInZoomControls(true);
				webview.getSettings().setDisplayZoomControls(true);
				webview.getSettings().setEnableSmoothTransition(true);
				webview.getSettings().setGeolocationEnabled(true);
				webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
				webview.getSettings().setLightTouchEnabled(true);
				webview.getSettings().setLoadWithOverviewMode(true);
				webview.getSettings().setLoadsImagesAutomatically(true);
				webview.getSettings().setPluginsEnabled(true);
				webview.getSettings().setSupportMultipleWindows(true);
				webview.getSettings().setSupportZoom(true);
				webview.getSettings().setUseWideViewPort(true);
				webview.getSettings().setPluginState(android.webkit.WebSettings.PluginState.ON_DEMAND);

				webview.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
				webview.setScrollbarFadingEnabled(false);
				webview.setHorizontalScrollBarEnabled(false);
	*/
				webview.loadUrl("http://localhost:" + http_port + "/m");
				Log.v(TAG, "WebView started successfully");
			}
		} else {
			Log.v(TAG, "could not open file" + file_to_open);
			mUtils.myToast("Could not open file " + file_to_open, Toast.LENGTH_SHORT);
			Log.v(TAG, "finishing WebActivity");
			//finish();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy() called");
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

	private class RadareWebViewClient extends WebViewClient {
		private boolean once = true;
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			once = true;
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String port = prefs.getString("r2argsHttp", "9090");
			mUtils.sleep (1);
			if (once) {
				view.loadUrl("http://localhost:" + port);
				once = false;
			}
			// reload page
			// retry in few seconds
			Log.v(TAG, "Error: radare2 webserver did not start");
			mUtils.myToast("Error: radare2 webserver did not start", Toast.LENGTH_LONG);
			//finish();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
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

	public String getLocalIpAddress() {
		try {
			String ipv4;
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4=inetAddress.getHostAddress())) {
						return ipv4;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
}
