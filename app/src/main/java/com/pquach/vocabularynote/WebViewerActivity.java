package com.pquach.vocabularynote;

import com.pquach.vocabularynote.R;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewerActivity extends Activity {

	WebView wvLongman;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wvLongman = new WebView(this);
		wvLongman.setWebViewClient(new WebViewClient(){
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(getApplicationContext(), description, Toast.LENGTH_LONG).show();
		    }});
		
		//-------Set the web page fit the screen and make the webview zoomable--------
		wvLongman.getSettings().setLoadWithOverviewMode(true);
		wvLongman.getSettings().setUseWideViewPort(true);
		wvLongman.getSettings().setBuiltInZoomControls(true);
		wvLongman.getSettings().setJavaScriptEnabled(true);
		
		// --------Get new word-------------
		String word = getIntent().getStringExtra("word");
		String url = getIntent().getStringExtra("url") + word.trim();
		//String url = "http://www.ldoceonline.com/search/?q=" + word.trim();
		wvLongman.loadUrl(url);
		setContentView(wvLongman);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_viewer, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_refresh:
				wvLongman.reload();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && wvLongman.canGoBack()) {
	    	wvLongman.goBack();
	        return true;
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
}
