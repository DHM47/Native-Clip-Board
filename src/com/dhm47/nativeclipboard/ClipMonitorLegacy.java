package com.dhm47.nativeclipboard;



import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;


public class ClipMonitorLegacy extends Service {
    private Context mctx;
    private ClipboardManager mClipboardManager;
	

	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate() {
        super.onCreate();
        mctx=this;
        Toast.makeText(mctx, "Clip Monitor Started", Toast.LENGTH_SHORT).show();
        if(mctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4).getBoolean("notification", true)){
        Notification.Builder mBuilder = new Notification.Builder(this)
		.setSmallIcon(R.drawable.ic_clipboard)
		.setContentTitle("ClipBoard Monitor")
		.setOngoing(true)
		.setWhen(System.currentTimeMillis());
        Notification notification ;//=mBuilder.build(); //new Notification(R.drawable.ic_launcher, "ClipBoard Monitor",System.currentTimeMillis());
		if (Build.VERSION.SDK_INT<16)notification=mBuilder.getNotification();
		else {notification=mBuilder.build();
				notification.priority=-2;}
        notification.flags=Notification.FLAG_ONLY_ALERT_ONCE|Notification.FLAG_ONGOING_EVENT;
        startForeground(1259, notification);}
        
        mClipboardManager =(ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }

    @Override
    public void onDestroy() {
        if (mClipboardManager != null) {
        	mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        super.onDestroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
		@Override
        public void onPrimaryClipChanged() {
        	Intent intent= new Intent(mctx, ClipMonitorService.class);
        	intent.putExtra("Package", "");
        	intent.putExtra("Clip",mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(mctx).toString());
        	intent.putExtra("Time",System.currentTimeMillis());
        	mctx.startService(intent);}
    };
}