package com.dhm47.nativeclipboard;




import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadCast extends BroadcastReceiver{

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		Intent intent = new Intent(ctx, ClipBoard.class);
		ctx.startService(intent);
		return;
	}
	
}
