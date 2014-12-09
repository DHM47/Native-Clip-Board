package com.dhm47.nativeclipboard;





import android.app.Activity;
import android.content.Context;
import android.os.Bundle;




public class Setting extends Activity{
    static Context ctx;
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
		ctx=this;
	}
	@Override
	public void onDestroy (){
		super.onDestroy();
	}
	public static Context getContext(){
		return ctx;
	}
}