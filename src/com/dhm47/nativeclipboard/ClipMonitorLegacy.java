package com.dhm47.nativeclipboard;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;


public class ClipMonitorLegacy extends Service {
    private Context mctx;
    private ClipboardManager mClipboardManager;
    private List<String> mClip = new ArrayList<String>();
	

	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate() {
        super.onCreate();
        mctx=this;
        Toast.makeText(mctx, "Clip Monitor Started", Toast.LENGTH_SHORT).show();
        if(mctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4).getBoolean("notification", true)){
        Notification.Builder mBuilder = new Notification.Builder(this)
		.setSmallIcon(R.drawable.ic_action_paste)
		.setContentTitle("ClipBoard Monitor")
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
        @SuppressWarnings("unchecked")
		@Override
        public void onPrimaryClipChanged() {
        	try {//Read
    			FileInputStream fis = mctx.openFileInput("Clips");
    			ObjectInputStream is = new ObjectInputStream(fis);
    			mClip =  (List<String>) is.readObject();
    			is.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} catch (ClassNotFoundException e) {
    			e.printStackTrace();
    		}
        	try {//Write
				ClipData clip = mClipboardManager.getPrimaryClip();
				String clips=clip.getItemAt(0).coerceToText(mctx).toString();
				if( !(mClip.contains(clips)) && !(clips.equals("//NATIVECLIPBOARDCLOSE//")) && !(clips.equals("")) ){
			      mClip.add(0,clips);
	              int history =mctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4).getInt("history", 25);
	              for (int x=mClip.size();x>history;x--){
	  				mClip.remove(x-1);} 
	              FileOutputStream fos = mctx.openFileOutput("Clips", Context.MODE_PRIVATE);
	              ObjectOutputStream os = new ObjectOutputStream(fos);
	              os.writeObject(mClip);
	              os.close();
	              //Toast.makeText(mctx,R.string.copied, Toast.LENGTH_SHORT).show();
	              mClip=null;}
			} catch (Exception e) {}
        	
		}
    };
}