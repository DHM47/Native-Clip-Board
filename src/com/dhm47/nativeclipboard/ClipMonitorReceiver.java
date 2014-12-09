package com.dhm47.nativeclipboard;





import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClipMonitorReceiver extends BroadcastReceiver{

	String pkg;
	String Clip;
	long Time;
	
	@Override
	public void onReceive(Context ctx, Intent arg1) {
		pkg=arg1.getStringExtra("Package");
		Clip=arg1.getStringExtra("Clip");
		Time=arg1.getLongExtra("Time", 0);
		
		Intent intent= new Intent(ctx, ClipMonitorService.class);
		intent.putExtra("Package", pkg);
		intent.putExtra("Clip",Clip);
		intent.putExtra("Time",Time);
		ctx.startService(intent);
	}
	
	
}

