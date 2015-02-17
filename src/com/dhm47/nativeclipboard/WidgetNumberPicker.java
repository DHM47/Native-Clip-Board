package com.dhm47.nativeclipboard;
/*
 * By zst123 from XHaloFloatingWindow
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class WidgetNumberPicker extends DialogPreference {
	
	private NumberPicker picker;
	int mDefaultValue;
	int mMinValue;
	int mMaxValue;
	String[] values = new String[21];
	SharedPreferences mPref;
	
	public WidgetNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.dialog_number_picker);
		mDefaultValue = (Integer.parseInt(attrs.getAttributeValue(null, "defaultValue")));
		mMinValue = (Integer.parseInt(attrs.getAttributeValue(null, "minimum")));
		mMaxValue = (Integer.parseInt(attrs.getAttributeValue(null, "maximum")));
	}
	
	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();
		mPref = getPreferenceManager().getSharedPreferences();
		picker = (NumberPicker) view.findViewById(R.id.number_picker);
		picker.setMaxValue(mMaxValue);
		picker.setMinValue(mMinValue);
		if(mMaxValue==21){
			for(int i = 0; i < 20; i++){
				values[i]=""+((i+1)*5);
			}
			values[20]=""+9999;
		picker.setDisplayedValues(values);
		}
		return view;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		if(mPref.getInt(getKey(), mDefaultValue)==9999)
		picker.setValue(21);
		else picker.setValue(mPref.getInt(getKey(), mDefaultValue)/5); 
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mPref.edit().putInt(getKey(), Integer.parseInt(values[picker.getValue()-1]) ).commit();
		}
	}
}