package com.dhm47.nativeclipboard;





import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClipMonitorReceiver extends BroadcastReceiver{

	String pkg;
	String Clip;
	
	@Override
	public void onReceive(Context ctx, Intent arg1) {
		pkg=arg1.getStringExtra("Package");
		Clip=arg1.getStringExtra("Clip");
		
		Intent intent= new Intent(ctx, ClipMonitorService.class);
		intent.putExtra("Package", pkg);
		intent.putExtra("Clip",Clip);
		ctx.startService(intent);
	}
	
	
}

