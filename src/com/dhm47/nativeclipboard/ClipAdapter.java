package com.dhm47.nativeclipboard;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ClipAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater inflater;
	public static List<String> mClips = new ArrayList<String>();
	//public static List<String> pClips = new ArrayList<String>();
	SharedPreferences setting ;
	
	public ClipAdapter(Context c){
		mContext = c;
		setting = mContext.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4);
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
	
	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public TextView getView(int position, View convertView, ViewGroup parent) {			
		TextView textView=new TextView(mContext);
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		textView=(TextView) inflater.inflate(R.layout.textview,null);
		textView.setText(mClips.get(position));
		if(ClipBoard.pinned.contains(mClips.get(position)))textView.setBackgroundColor(setting.getInt("pincolor",0xFFCF5300));
		else textView.setBackgroundColor(setting.getInt("clpcolor",0xFFFFBB22));
		textView.setTextColor(setting.getInt("txtcolor",0xffffffff));
		textView.setTextSize((float)(setting.getInt("txtsize",  20)));
		return textView;
	}

}