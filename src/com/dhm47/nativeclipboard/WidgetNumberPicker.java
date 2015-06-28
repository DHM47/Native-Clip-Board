package com.dhm47.nativeclipboard;
/*
 * By zst123 from XHaloFloatingWindow
 */

import java.lang.reflect.Field;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class WidgetNumberPicker extends DialogPreference {
	
	private NumberPicker picker;
	int mDefaultValue;
	int mMinValue;
	int mMaxValue;
	String[] values = new String[21];
	SharedPreferences mPref;
	int mColor;
	View mView;
	
	public WidgetNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.dialog_number_picker);
		mDefaultValue = (Integer.parseInt(attrs.getAttributeValue(null, "defaultValue")));
		mMinValue     = (Integer.parseInt(attrs.getAttributeValue(null, "minimum")));
		mMaxValue     = (Integer.parseInt(attrs.getAttributeValue(null, "maximum")));
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
		mColor = value.data;
        

		
	}
	
	
	
	@SuppressLint("InflateParams")
	@Override
	protected void showDialog(Bundle state) {

		mPref = getPreferenceManager().getSharedPreferences();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getTitle())
		.setPositiveButton(getPositiveButtonText(), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if(mMaxValue==21){
				mPref.edit().putInt(getKey(), Integer.parseInt(values[picker.getValue()-1]) ).commit();
				}else mPref.edit().putInt(getKey(), picker.getValue()).commit();
			
				
			}
		})
		.setNegativeButton(getNegativeButtonText(), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
//		.callback(new ButtonCallback() {
//
//			@Override
//			public void onPositive(MaterialDialog dialog) {
//				if(mMaxValue==21){
//				mPref.edit().putInt(getKey(), Integer.parseInt(values[picker.getValue()-1]) ).commit();
//				}else mPref.edit().putInt(getKey(), picker.getValue()).commit();
//			}
//		
//			//super.onDialogClosed(positiveResult);
//			
//		});

		LayoutInflater inflater = LayoutInflater.from(getContext());
        mView=inflater.inflate(R.layout.dialog_number_picker, null);
		picker = (NumberPicker) mView.findViewById(R.id.number_picker);
		picker.setMaxValue(mMaxValue);
		picker.setMinValue(mMinValue);
		try {
		    Field f1 = Class.forName("android.widget.NumberPicker").getDeclaredField("mSelectionDivider");
		    f1.setAccessible(true);
		    ((Drawable) f1.get(picker)).setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		if(mMaxValue==21){
			for(int i = 0; i < 20; i++){
				values[i]=""+((i+1)*5);
			}
			values[20]=""+9999;
		picker.setDisplayedValues(values);
		}
		
        onBindDialogView(mView);
        builder.setView(mView);

        builder.show();
	}
	
	@Override
	protected void onBindDialogView(View view) {
		//super.onBindDialogView(view);
		if(mMaxValue==21){
			if(mPref.getInt(getKey(), mDefaultValue)==9999)
				picker.setValue(21);
			else picker.setValue(mPref.getInt(getKey(), mDefaultValue)/5);
		}else picker.setValue(mPref.getInt(getKey(), mDefaultValue));

	}
}