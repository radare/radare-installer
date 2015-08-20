/*
radare2 installer for Android
(c) 2012 Pau Oliva Fora <pof[at]eslack[dot]org>
    2015 pancake <pancake[at]nopcode[dot]org>
*/
package org.radare2.installer;

import org.radare2.installer.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

import android.view.KeyEvent;
import android.webkit.WebView;
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
		boolean http_public = prefs.getBoolean("http_public", false);
		String http_port = prefs.getString("http_port", "9090");

		String port = http_port;
		if (http_port.equals("")) {
			http_port = " -e http.port=9090 ";
		} else {
			http_port = " -e http.port="+http_port;
		}
		String http_eval = "";
		if (http_public) {
			//http_eval = "-e http.public=1";
			// after 0.9.8
			http_eval = " -e http.bind=public ";
			String localip = getLocalIpAddress();
			if (localip != null) {
				mUtils.myToast("r2 http server\n" + localip + ":" + port, Toast.LENGTH_LONG);
				Log.v(TAG, "ip address: " + localip);
			}
		}
		Log.v(TAG, "http_eval: " + http_eval);

		String output = mUtils.exec("/data/data/org.radare2.installer/radare2/bin/radare2 " + 
			http_port + http_eval + " -c=h " + file_to_open + " &");
		Log.v(TAG, "radare2 started");

		// if radare2 is launched in background we need to wait
		// for it to start before opening the webview
/*
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
                        e.printStackTrace();
                }
*/
		if (true) { //RootTools.isProcessRunning("radare2")) {
			String open_mode = mUtils.GetPref("open_mode");
			if (open_mode.equals("browser")) {
				String url = "http://localhost:"+port;
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.setData(Uri.parse(url));
				startActivity(i);
				Log.v(TAG, "Browser started");
			}
			if (open_mode.equals("web")) {
				try {
					webview = (WebView) findViewById(R.id.webview);
					webview.setWebViewClient(new RadareWebViewClient());
					webview.setWebChromeClient(new WebChromeClient());
					webview.getSettings().setJavaScriptEnabled(true);
					webview.getSettings().setBuiltInZoomControls(true);
					webview.getSettings().setSupportZoom(true);
					webview.getSettings().setUseWideViewPort(true);
					webview.getSettings().setLoadWithOverviewMode(true);
				} catch (Exception e) {
				}
	/*
				webview.getSettings().setAllowFileAccess(true);
				webview.getSettings().setDomStorageEnabled(true);
				webview.getSettings().setJavaScriptEnabled(true);
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
				webview.getSettings().setAllowFileAccess(true);
				//    webview.getSettings().setAllowFileAccessFromFileURLs(true);
				//    webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
				webview.getSettings().setAppCacheEnabled(true);
				webview.getSettings().setBuiltInZoomControls(true);
				webview.getSettings().setDatabaseEnabled(true);
				webview.getSettings().setDisplayZoomControls(true);
				webview.getSettings().setDomStorageEnabled(true);
				webview.getSettings().setEnableSmoothTransition(true);
				webview.getSettings().setGeolocationEnabled(true);
				webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
				webview.getSettings().setJavaScriptEnabled(true);
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
				webview.loadUrl("http://localhost:"+port);
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
	public void onStop() {
		super.onStop();
		Log.v(TAG, "onStop() called");
		mUtils.killradare();
	}

	private class RadareWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
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
