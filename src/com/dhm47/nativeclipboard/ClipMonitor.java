package com.dhm47.nativeclipboard;




import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ClipMonitor extends BroadcastReceiver{

	String pkg;
	String Clip;
	private List<String> mClip = new ArrayList<String>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context ctx, Intent arg1) {
		pkg=arg1.getStringExtra("Package");
		Clip=arg1.getStringExtra("Clip");
		
		
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
			}else if(Clip.equals("")){
			}else{
		      mClip.add(0,Clip);
              int history =ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4).getInt("history", 25);
              for (int x=mClip.size();x>history;x--){
  				mClip.remove(x-1);} 
              FileOutputStream fos = ctx.openFileOutput("Clips", Context.MODE_PRIVATE);
              ObjectOutputStream os = new ObjectOutputStream(fos);
              os.writeObject(mClip);
              os.close();
              //Toast.makeText(ctx,R.string.copied, Toast.LENGTH_SHORT).show();
              mClip=null;}
		} catch (Exception e) {}
		}
	
		return;
	}
	
	private static boolean isBlacklisted(String currentPkg,Context context){
        SharedPreferences prefs = context.getSharedPreferences("com.dhm47.nativeclipboard_blacklist", 4);
		//Get the pref from our custom preference 
		int size = prefs.getInt("items" + "_size", 0);
		if(size != 0) {
			for(int i = 0; i < size; i++){
				String pkg = prefs.getString("items" + "_" + i, "");
				if(pkg.equals(currentPkg)){
					//Toast.makeText(context, "Black listed", Toast.LENGTH_SHORT).show();
					return true;}
			}
		}
		//Toast.makeText(context, "Not Black listed", Toast.LENGTH_SHORT).show();
		return false;
	}
}

