package com.dhm47.nativeclipboard;




import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		//TODO hook this to see if enabled
		ctx.startService(new Intent(ctx, ClipMonitor.class));
		((ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Text", ""));
		return;
	}
	
}
