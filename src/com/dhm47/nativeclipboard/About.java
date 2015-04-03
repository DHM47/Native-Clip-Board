package com.dhm47.nativeclipboard;

import android.webkit.WebViewFragment;


public class About extends WebViewFragment {
	
	@Override
	public void onStart() {
		super.onStart();
		getWebView().loadUrl("file:///android_asset/html/about.html");
	}
	
}