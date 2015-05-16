package com.dhm47.nativeclipboard;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dhm47.nativeclipboard.comparators.NewFirst;
import com.dhm47.nativeclipboard.comparators.PinnedFirst;
import com.dhm47.nativeclipboard.comparators.PinnedLast;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;




public class ClipMonitorService extends Service{

	String pkg;
	String clip;
	long time;
	private static List<Clip> mClip = new ArrayList<Clip>();
	
	@SuppressWarnings("unchecked")
	@Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    pkg=intent.getStringExtra("Package");
		clip=intent.getStringExtra("Clip");
		time=intent.getLongExtra("Time", 0);
		Context ctx =this;
		Clip nClip=new Clip(time, clip, "", false);
		if(!isBlacklisted(pkg, ctx)){
			
    	try {//Read Clips
			FileInputStream fis = ctx.openFileInput("Clips2.9");
			ObjectInputStream is = new ObjectInputStream(fis);
			mClip =  (List<Clip>) is.readObject();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	try {//Write
    		int conatains=Clip.contains(mClip, nClip);
			if(conatains>=0){
				mClip.get(conatains).setTime(time);
				FileOutputStream fos = ctx.openFileOutput("Clips2.9", Context.MODE_PRIVATE);
	            ObjectOutputStream os = new ObjectOutputStream(fos);
	              os.writeObject(mClip);
	              os.close();
			}else if( (conatains==-1) && !(clip.equals("//NATIVECLIPBOARDCLOSE//")) && !(clip.equals("")) ){
		      mClip.add(0,nClip);
              int history =ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4).getInt("history", 25);
              for (int x=mClip.size();mClip.size()>history;x--){
  				if(!mClip.get(x-1).isPinned())mClip.remove(x-1);
  					} 
              String sort=ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4).getString("sort", "newfirst");
				if(sort.equals("newfirst")){
					Collections.sort(mClip, new NewFirst());
				}else if(sort.equals("pinnedfirst")){
					Collections.sort(mClip, new PinnedFirst());
				}else if(sort.equals("pinnedlast")){
					Collections.sort(mClip, new PinnedLast());
				}
              FileOutputStream fos = ctx.openFileOutput("Clips2.9", Context.MODE_PRIVATE);
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
