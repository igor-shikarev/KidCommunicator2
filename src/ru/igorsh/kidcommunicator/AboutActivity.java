package ru.igorsh.kidcommunicator;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		// загрузка текста
		WebView lWebView = (WebView) findViewById(R.id.webView);
		lWebView.loadUrl("file:///android_res/raw/about.html");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem sender) {
		// Операции для выбранного пункта меню
		switch (sender.getItemId()) {
			case R.id.mi_about:
				showAbout();
				return true;
			case R.id.mi_settings:
				showSettings();
				return true;
			case R.id.mi_donation:
				showDonation();
				return true;        
			default:
				return super.onOptionsItemSelected(sender);
		}
	}		
	
	private void showAbout() {
		Intent intent = new Intent(AboutActivity.this, AboutActivity.class);
		startActivity(intent);
	}
	
	private void showSettings() {
		Intent intent = new Intent(AboutActivity.this, SettingsActivity.class);
		startActivity(intent);
	}
  
  private void showDonation() {
    Intent intent = new Intent(AboutActivity.this, DonateActivity.class);
    startActivity(intent);
	}  
}
