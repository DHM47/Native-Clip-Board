package com.dhm47.nativeclipboard;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ClipAdapter extends BaseAdapter {
	private Context mContext;
	private static ClipboardManager mClipboardManager;
	private static LayoutInflater inflater;
	public static List<String> mClips = new ArrayList<String>();
	//public static List<String> pClips = new ArrayList<String>();
	static SharedPreferences setting ;
	static TextView textView;
	
	public ClipAdapter(Context c){
		mContext = c;
		
		}

	@Override
	public int getCount() {
		return mClips.size();
	}

	@Override
	public Object getItem(int position) {
		return mClips.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {			
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mClipboardManager =(ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
		setting = mContext.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4);
		textView=new TextView(mContext);
		textView=(TextView) inflater.inflate(R.layout.textview,null);
		if(ClipBoard.pinned.contains(mClips.get(position)))textView.setBackgroundColor(setting.getInt("pincolor",0xFFCF5300));
		else textView.setBackgroundColor(setting.getInt("clpcolor",0xFFFFBB22));
		textView.setTextColor(setting.getInt("txtcolor",0xffffffff));
		textView.setTextSize((float)(setting.getInt("txtsize",  20)));
		textView.setOnLongClickListener(new OnLongClickListener() {@Override public boolean onLongClick(View v) {return false;}});
		textView.setOnTouchListener(new SwipeDismissTouchListener(textView,
                null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                    	if(ClipBoard.pinned.contains(mClips.get(position)))return false;
                        else return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                    	ClipBoard.backupS=ClipAdapter.mClips.get(position);
        				ClipBoard.backupP=position;
        				ClipAdapter.mClips.remove(position);
        				notifyDataSetChanged();
                    }
                }));
		textView.setText(mClips.get(position));
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", ClipAdapter.mClips.get(position)));		
				 mContext.stopService(new Intent(mContext, ClipBoard.class));
			}
		});
		if(ClipBoard.pinned.contains(mClips.get(position)))
			textView.setBackgroundColor(setting.getInt("pincolor",0xFFCF5300));
		
		return textView;
	}

}