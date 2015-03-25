package com.dhm47.nativeclipboard;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;

import com.dhm47.nativeclipboard.comparators.NewFirst;
import com.dhm47.nativeclipboard.comparators.PinnedFirst;
import com.dhm47.nativeclipboard.comparators.PinnedLast;

public class SettingsListFragment extends PreferenceFragment {
	private static Context ctx;
    Toolbar mToolbar;
	private Callbacks mCallbacks = sDummyCallbacks;
	private List<Clip> mClip = new ArrayList<Clip>();
    
    public interface Callbacks {
		/*
		 * Callback for when an item has been selected.
		 */
		public void onCatagorySelected(String key);
	}
    private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onCatagorySelected(String key) {
		}
	};
	
	public SettingsListFragment() {
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=getActivity();
        addPreferencesFromResource(R.xml.pref_main);
        findPreference("sort").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
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
				String sort=(String) newValue;
				if(sort.equals("newfirst")){
					Collections.sort(mClip, new NewFirst());
				}else if(sort.equals("pinnedfirst")){
					Collections.sort(mClip, new PinnedFirst());
				}else if(sort.equals("pinnedlast")){
					Collections.sort(mClip, new PinnedLast());
				}
		    	try {//Write
		              FileOutputStream fos = ctx.openFileOutput("Clips2.9", Context.MODE_PRIVATE);
		              ObjectOutputStream os = new ObjectOutputStream(fos);
		              os.writeObject(mClip);
		              os.close();
				} catch (Exception e) {}
				return true;
			}
		});
    }
    
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}
    
    @Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
    		Preference preference) {
    	if(!preference.getKey().equals("sort"))
    	mCallbacks.onCatagorySelected(preference.getKey());
    	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    
}