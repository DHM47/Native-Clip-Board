package com.dhm47.nativeclipboard;





import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;




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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.setting_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_test:
	        	Intent intent = new Intent(ctx, ClipBoard.class);
				ctx.startActivity(intent);
				return true;
	        case R.id.action_support:
	        	Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/native-clip-board-beta-t2784682"));
				ctx.startActivity(intent1);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}