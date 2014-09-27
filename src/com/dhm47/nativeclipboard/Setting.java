package com.dhm47.nativeclipboard;





import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;




public class Setting extends PreferenceActivity {
    Context ctx;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx=this;
		addPreferencesFromResource(R.layout.preference_headers);
		findPreference("test").setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(ctx, ClipBoard.class);
				ctx.startActivity(intent);
				return true;
			}
		});
		findPreference("blacklist").setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(ctx, Blacklist.class);
				ctx.startActivity(intent);
				return true;
			}
		});
		findPreference("xda").setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/native-clip-board-beta-t2784682"));
				ctx.startActivity(intent);
				return true;
			}
		});
				
	}
	@Override
	public void onDestroy (){
		super.onDestroy();
	}
	
}