/*
 * Copyright (C) 2013 XuiMod
 * Based on source obtained from the ParanoidAndroid Project, Copyright (C) 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dhm47.nativeclipboard;

import java.util.ArrayList;
import java.util.List;

import com.dhm47.nativeclipboard.ApplicationsDialog.AppAdapter;
import com.dhm47.nativeclipboard.ApplicationsDialog.AppItem;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

@SuppressLint("WorldReadableFiles")
public class Blacklist extends PreferenceActivity {
    // TODO : Rearrange + Cleanup code.
	
	public static Dialog dialog = null;
    private static final int MENU_ADD = 0;
    private static final int MENU_HELP = 1;
    
    private PreferenceScreen mRoot;
    private List<ResolveInfo> mInstalledApps;
    private AppAdapter mAppAdapter;
    private OnPreferenceClickListener mOnItemClickListener = new OnPreferenceClickListener(){
    	@Override
    	public boolean onPreferenceClick(Preference arg0) {
    		mRoot.removePreference(arg0);
    		savePreferenceItems(false);
    		invalidateOptionsMenu();
    		return false;
    	}
    };

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mInstalledApps = this.getPackageManager().queryIntentActivities(mainIntent, 0);
        ApplicationsDialog appDialog = new ApplicationsDialog();
        mAppAdapter = appDialog.createAppAdapter(this, mInstalledApps);
        mAppAdapter.update();
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));
        mRoot = getPreferenceScreen();
        loadPreferenceItems();
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ADD, 0, R.string.add)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        menu.add(Menu.NONE, MENU_HELP, 1, R.string.help)
        	.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_HELP:
        	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        	alertDialog.setTitle(R.string.help);
        	alertDialog.setMessage(getString(R.string.blacklist_help));
        	alertDialog.show();
        	break;
        case MENU_ADD:
            	
        	dialog = new Dialog(this);
        	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        	dialog.setContentView(R.layout.pref_app_picker);
        	
        	final ListView list = (ListView) dialog.findViewById(R.id.listView1);
        	list.setAdapter(mAppAdapter);
        	list.setOnItemClickListener(new OnItemClickListener() {
        		@Override
        		public void onItemClick(AdapterView<?> v, View arg1, int pos, long arg3) {
        			AppItem info = (AppItem) v.getItemAtPosition(pos);
        			for(int i = 0; i < mRoot.getPreferenceCount(); i++){
        				if(mRoot.getPreference(i).getSummary()
        						.equals(info.packageName)){
        					return;
        				}
        			}
        			Preference item = new Preference(getBaseContext());
        			item.setTitle(info.title);
        			item.setSummary(info.packageName);
        			item.setIcon(info.icon);
        			item.setOnPreferenceClickListener(mOnItemClickListener);
        			mRoot.addPreference(item);
        			savePreferenceItems(true);
        			invalidateOptionsMenu();
        			dialog.cancel();
        		}
        	});
        	
        	final Button searchButton = (Button) dialog.findViewById(R.id.searchButton);
        	final EditText inputSearch = (EditText) dialog.findViewById(R.id.search);
        	searchButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					dialog.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
					mAppAdapter.getFilter().filter(inputSearch.getText().toString());
				}
        	});
        	           
        	dialog.show();
        	mAppAdapter.getFilter().filter("");
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void savePreferenceItems(boolean create){
        ArrayList<String> items = new ArrayList<String>();
        for(int i = 0; i < mRoot.getPreferenceCount(); i++){
            String packageName = mRoot.getPreference(i)
                    .getSummary().toString();
            items.add(packageName);
        }
        saveArray(items.toArray(new String[items.size()]), "items", this);
    }
    
    private static boolean saveArray(String[] array, String arrayName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.dhm47.nativeclipboard_blacklist", 4);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putInt(arrayName +"_size", array.length);
        for(int i = 0; i<array.length; i++) {
            editor.putString(arrayName + "_" + i, array[i]);
        }
        return editor.commit();
    }
    
    private static String[] loadArray(String arrayName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.dhm47.nativeclipboard_blacklist", 4);
        int size = prefs.getInt(arrayName + "_size", 0);
        String[] array = null;
        if(size != 0) {
            array = new String[size];
            for(int i = 0; i<size; i++){
                array[i] = prefs.getString(arrayName + "_" + i, null);
            }
        }
        return array;
    }
    
    private static Drawable getApplicationIconDrawable(String packageName, Context context){
        Drawable appIcon = null;
        try {
            appIcon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (Exception e) {
        }
        return appIcon;
    }

    private static String getApplicationName(String packageName, Context context){
    	try {
    		PackageManager pm = context.getPackageManager();
            return (String) (pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)));
        } catch (final Exception e) {
        }
    	return context.getString(R.string.unknown);
    }
    
    private void loadPreferenceItems(){
        String[] packages = loadArray("items", this);
        if(packages == null) return;
        for(String packageName : packages){
            Preference app = new Preference(this);
            app.setTitle(getApplicationName(packageName, this));
            app.setSummary(packageName);
            app.setIcon(getApplicationIconDrawable(packageName, this));
            app.setOnPreferenceClickListener(mOnItemClickListener);
            mRoot.addPreference(app);
        }
    }
}
