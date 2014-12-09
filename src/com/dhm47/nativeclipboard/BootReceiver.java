package com.dhm47.nativeclipboard;




import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		((ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Text", ""));
		if(ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4).getBoolean("monitorservice", false)){
			ctx.startService(new Intent(ctx, ClipMonitorLegacy.class));
		}
		return;
	}
	
}
