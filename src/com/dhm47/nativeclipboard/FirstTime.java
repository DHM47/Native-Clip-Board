package com.dhm47.nativeclipboard;





import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;




public class FirstTime extends PreferenceActivity {
    Context ctx;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent =new Intent(this, ClipMonitor.class);
		startService(intent);
		ctx=this;
		addPreferencesFromResource(R.layout.preference_headers);
		findPreference("test").setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(ctx, ClipBoard.class);
				ctx.startService(intent);
				return true;
			}
		});
		SharedPreferences setting = ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4);
		setting.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if(key.equals("notification")){
					ctx.stopService(intent);
					ctx.startService(intent);
				}
				
			}
		});	
	}
	@Override
	public void onDestroy (){
		super.onDestroy();
	}
	
}