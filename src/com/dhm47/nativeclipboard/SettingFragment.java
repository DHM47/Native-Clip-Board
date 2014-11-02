package com.dhm47.nativeclipboard;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.preference_headers);
		findPreference("test").setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), ClipBoard.class);
				getActivity().startActivity(intent);
				return true;
			}
		});
		findPreference("blacklist").setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), Blacklist.class);
				getActivity().startActivity(intent);
				return true;
			}
		});
		findPreference("monitorservice").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("blacklist").setEnabled(!((Boolean) newValue));
				return true;
			}
		});
    }
    
    /*
     * Adding Up (carrot) button in setting sub-menus
     * Thanks to jimmithy
     * http://stackoverflow.com/a/16800527
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        // If the user has clicked on a preference screen, set up the action bar
        if (preference instanceof PreferenceScreen) {
            initializeActionBar((PreferenceScreen) preference);
        }

        return false;
    } 
    public static void initializeActionBar(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();

        if (dialog != null) {
            // Inialize the action bar
            dialog.getActionBar().setDisplayHomeAsUpEnabled(true);

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
        }
    }
}