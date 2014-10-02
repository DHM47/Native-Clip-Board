package com.dhm47.nativeclipboard;





import android.app.Activity;
import android.os.Bundle;




public class Setting extends Activity{
    
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();

	}
	@Override
	public void onDestroy (){
		super.onDestroy();
	}
	
}