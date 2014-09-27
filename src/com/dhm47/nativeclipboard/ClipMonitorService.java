package com.dhm47.nativeclipboard;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;




public class ClipMonitorService extends Service{

	String pkg;
	String Clip;
	private static List<String> mClip = new ArrayList<String>();
	
	@SuppressWarnings("unchecked")
	@Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    pkg=intent.getStringExtra("Package");
		Clip=intent.getStringExtra("Clip");
		Context ctx =this;

		if(!isBlacklisted(pkg, ctx)){
			
    	try {//Read Clips
			FileInputStream fis = ctx.openFileInput("Clips");
			ObjectInputStream is = new ObjectInputStream(fis);
			mClip =  (List<String>) is.readObject();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	try {//Write
			if(mClip.contains(Clip)){
			}else if(Clip.equals("//NATIVECLIPBOARDCLOSE//")){
			}else{
		      mClip.add(0,Clip);
              int history =ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4).getInt("history", 25);
              for (int x=mClip.size();x>history;x--){
  				mClip.remove(x-1);} 
              FileOutputStream fos = ctx.openFileOutput("Clips", Context.MODE_PRIVATE);
              ObjectOutputStream os = new ObjectOutputStream(fos);
              os.writeObject(mClip);
              os.close();
              }
		} catch (Exception e) {}
		}		
		stopSelf();
	    return Service.START_NOT_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	    return null;
	  }

	  private static boolean isBlacklisted(String currentPkg,Context context){
	        SharedPreferences prefs = context.getSharedPreferences("com.dhm47.nativeclipboard_blacklist", 4);
			//Get the pref from our custom preference 
			int size = prefs.getInt("items" + "_size", 0);
			if(size != 0) {
				for(int i = 0; i < size; i++){
					String pkg = prefs.getString("items" + "_" + i, "");
					if(pkg.equals(currentPkg)){
						return true;}
				}
			}
			return false;
		}
	
	
}
