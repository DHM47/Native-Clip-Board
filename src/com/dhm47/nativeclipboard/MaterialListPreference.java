package com.dhm47.nativeclipboard;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Method;

/**
 * @author Marc Holder Kluver (marchold), Aidan Follestad (afollestad)
 */
public class MaterialListPreference extends ListPreference {

    private Context context;
    private AppCompatDialog mDialog;

    public MaterialListPreference(Context context) {
        super(context);
        init(context);
    }

    public MaterialListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
            setWidgetLayoutResource(0);
    }

//    @Override
//    public void setEntries(CharSequence[] entries) {
//        super.setEntries(entries);
//        if (mDialog != null)
//            mDialog.setItems(entries);
//    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    protected void showDialog(Bundle state) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }

        int preselect = findIndexOfValue(getValue());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getDialogTitle())
                .setMessage(getDialogMessage())
                .setNegativeButton(getNegativeButtonText(), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mDialog.dismiss();
						
					}
				})
                .setSingleChoiceItems(getEntries(),preselect, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which >= 0 && getEntryValues() != null) {
                          String value = getEntryValues()[which].toString();
                          if (callChangeListener(value) && isPersistent())
                              setValue(value);
                          mDialog.dismiss();
                      }
						
					}
				}).setInverseBackgroundForced(true);
                
                //.autoDismiss(true) // immediately close the dialog after selection
//                .itemsCallbackSingleChoice(preselect, new MaterialDialog.ListCallbackSingleChoice() {
//                    @Override
//                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
//                        onClick(null, DialogInterface.BUTTON_POSITIVE);
//                        if (which >= 0 && getEntryValues() != null) {
//                            String value = getEntryValues()[which].toString();
//                            if (callChangeListener(value) && isPersistent())
//                                setValue(value);
//                        }
//                        return true;
//                    }
//                });

        final View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.setView(contentView);
        } else {
            builder.setMessage(getDialogMessage());
        }

        PreferenceManager pm = getPreferenceManager();
        try {
            Method method = pm.getClass().getDeclaredMethod(
                    "registerOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDialog = builder.create();
        if (state != null)
            mDialog.onRestoreInstanceState(state);
        mDialog.show();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    
    @Override
    public void setValue(String value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super.setValue(value);
        } else {
            String oldValue = getValue();
            super.setValue(value);
            if (!TextUtils.equals(value, oldValue))
                notifyChanged();
        }
    }
}