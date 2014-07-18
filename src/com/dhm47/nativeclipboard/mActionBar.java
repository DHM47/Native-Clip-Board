package com.dhm47.nativeclipboard;



import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.Selection;
import android.text.Spannable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class mActionBar {
	static Context ctx;
	final int id =1259;
	private static TextView mTextView;
	private ClipboardManager mClipboardManager;	
	private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
	
	
	public mActionBar(TextView textView, Context mContext) {
		mTextView=textView;
		ctx=mContext;
	}

	

	public void add() {
		mTextView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				if((menu.findItem(android.R.id.paste))!=null){
				menu.add(android.view.Menu.NONE, id,android.view.Menu.NONE, "CB");
				menu.findItem(id).setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
				//menu.findItem(id).setIcon(menu.findItem(android.R.id.paste).getIcon());
				}
				
				
				
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch(item.getItemId()) {
		        case id:
		        	open();
		            return true;
				}
		        return false;
			}

			
		});
		
	}
	
	public void open() {
		
		//int[] location = new int[2];
		//mTextView.getLocationOnScreen(location);
		final int start=mTextView.getSelectionStart();
		final int end=mTextView.getSelectionEnd();
		/*String x=""+location[0];
		String y=""+location[1];
		Toast.makeText(ctx, "X="+x+" Y="+y, Toast.LENGTH_SHORT).show();*/
		Intent intent = new Intent();
		intent.setAction("DHM47.Xposed.ClipBoard");
		//intent.putExtra("Keyheight", location[1]+mTextView.getHeight());
		ctx.sendBroadcast(intent);
		
		mClipboardManager =(ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
		
		mOnPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
            	try {
					mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
				} catch (Exception e1) {
					Toast.makeText(ctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
					e1.printStackTrace();
				}
            	try {
            		if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(ctx).toString().equals(""));
            		else{   		   mTextView.setText(mTextView.getText().subSequence(0, start).toString()
            						  +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(ctx).toString()
            						  +mTextView.getText().subSequence(end, mTextView.getText().length()).toString());
            		Selection.setSelection((Spannable) mTextView.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(ctx).length());}
				} catch (Throwable e) {
					Toast.makeText(ctx, "pasting went wrong", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
        }};
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		
	}
	
}
