package com.dhm47.nativeclipboard;





import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Window;




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
				ctx.startService(intent);
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
		SharedPreferences setting = ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4);
		boolean firstrun =setting.getBoolean("firstrun", true);
		if (firstrun){
			setting.edit().putBoolean("firstrun", false).commit();
			AlertDialog.Builder firsttime =new AlertDialog.Builder(this, 16973947);
			firsttime.setMessage("This is an Alpha build so please provide feedback on the XDA thread.\n\n\n*Add: Blacklisting(ex password managers).\n\n*Change: Use Xposed to get copied text.\n\n"
					+ "*Change: Single press paste(for testing) Also porved more compatiable(ex ES Explorer)");
			firsttime.setNeutralButton("OK", null);
			AlertDialog alert1 = firsttime.create();
			alert1.requestWindowFeature(Window.FEATURE_NO_TITLE);
			alert1.show();
			
		
		}
		
	}
	@Override
	public void onDestroy (){
		super.onDestroy();
	}
	
}