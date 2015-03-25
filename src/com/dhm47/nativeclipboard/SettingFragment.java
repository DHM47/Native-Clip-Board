package com.dhm47.nativeclipboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;

public class SettingFragment extends PreferenceFragment {
	private static Context ctx;
    Toolbar mToolbar;

    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=getActivity();

        // Load the preferences from an XML resource
        String category = getArguments().getString("category");
        if (category != null) {
            if (category.equals("theme")) {
                addPreferencesFromResource(R.xml.pref_theme);
            } else if (category.equals("size")) {
                addPreferencesFromResource(R.xml.pref_sizes);
            }else if (category.equals("advanced")){
            	addPreferencesFromResource(R.xml.pref_advanced);
            	findPreference("monitorservice").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        			
        			@Override
        			public boolean onPreferenceChange(Preference preference, Object newValue) {
        				//findPreference("blacklist").setEnabled(!((Boolean) newValue));
        				if((Boolean) newValue)ctx.startService(new Intent(ctx, ClipMonitorLegacy.class));
        				else ctx.stopService(new Intent(ctx, ClipMonitorLegacy.class));
        				return true;
        			}
        		});
            }
        }
        /*addPreferencesFromResource(R.layout.preference_fragment);
		findPreference("blacklist").setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), Blacklist.class);
				ctx.startActivity(intent);
				return true;
			}
		});
		findPreference("monitorservice").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("blacklist").setEnabled(!((Boolean) newValue));
				if((Boolean) newValue)ctx.startService(new Intent(ctx, ClipMonitorLegacy.class));
				return true;
			}
		});
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
    
    /*
     * Adding Up (carrot) button in setting sub-menus
     * Thanks to jimmithy
     * http://stackoverflow.com/a/16800527
     *
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        // If the user has clicked on a preference screen, set up the action bar
        if (preference instanceof PreferenceScreen) {
        	
    		getActivity().getFragmentManager().beginTransaction().replace(R.id.content_frame, this).commit();

        	//initializeActionBar((PreferenceScreen) preference);
        }

        return false;
    } 
    /*public static void initializeActionBar(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();

        if (dialog != null) {
            // Inialize the action bar
            dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
    		//dialog.getActionBar().setIcon(new ColorDrawable(ctx.getResources().getColor(android.R.color.transparent)));

            // Apply custom home button area click listener to close the PreferenceScreen because PreferenceScreens are dialogs which swallow
            // events instead of passing to the activity
            // Related Issue: https://code.google.com/p/android/issues/detail?id=4611
            View homeBtn = dialog.findViewById(android.R.id.home);

            if (homeBtn != null) {
                OnClickListener dismissDialogClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                };

                // Prepare yourselves for some hacky programming
                ViewParent homeBtnContainer = homeBtn.getParent();

                // The home button is an ImageView inside a FrameLayout
                if (homeBtnContainer instanceof FrameLayout) {
                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

                    if (containerParent instanceof LinearLayout) {
                        // This view also contains the title text, set the whole view as clickable
                        ((LinearLayout) containerParent).setOnClickListener(dismissDialogClickListener);
                    } else {
                        // Just set it on the home button
                        ((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
                    }
                } else {
                    // The 'If all else fails' default case
                    homeBtn.setOnClickListener(dismissDialogClickListener);
                }
            }    
        }*/
    }
}