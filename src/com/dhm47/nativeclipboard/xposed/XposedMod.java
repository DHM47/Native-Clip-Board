package com.dhm47.nativeclipboard.xposed;


import com.dhm47.nativeclipboard.mActionBar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class XposedMod implements IXposedHookZygoteInit,IXposedHookLoadPackage,IXposedHookInitPackageResources {
	public static Context ctx;
	private Context CBMctx;
	private String pkg;
	static mActionBar actionBar;
	//static String MODULE_PATH;
	static Menu menu;
	final int id=1259;
	static MethodHookParam mparam;
	private ClipboardManager mClipboardManager;	
	private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		//MODULE_PATH =startupParam.modulePath;
		XposedHelpers.findAndHookMethod(TextView.class, "onFocusChanged", boolean.class, int.class,	Rect.class, new XC_MethodHook(){
			@Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				TextView textView = (TextView) param.thisObject;
				ctx=textView.getContext();
				boolean isEditText = textView instanceof EditText;
				if(isEditText){
				actionBar = new mActionBar(textView,ctx);
				actionBar.add();}
			}
		});
		XposedHelpers.findAndHookConstructor(ClipboardManager.class,Context.class,Handler.class, new XC_MethodHook(){
			@Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				CBMctx=(Context) param.args[0];
				//Log.d("NativeClipBoard", "got context");
				pkg=CBMctx.getPackageName();
				//Log.d("NativeClipBoard", pkg);
			}
		});
		XposedHelpers.findAndHookMethod(ClipboardManager.class, "setPrimaryClip", ClipData.class, new XC_MethodHook(){
			@Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				ClipData clip=(ClipData) param.args[0];
				Log.d("NativeClipBoard", pkg+" copied"+clip.getItemAt(0).coerceToText(CBMctx));
				Intent intent = new Intent();
				intent.setAction("DHM47.Xposed.ClipBoardMonitor");
				intent.putExtra("Package", pkg);
				intent.putExtra("Clip",clip.getItemAt(0).coerceToText(CBMctx));
				ctx.sendBroadcast(intent);
			}
		});
		
				
	}
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
	    resparam.res.hookLayout("android", "layout", "text_edit_action_popup_text", new XC_LayoutInflated() {
	        @Override
	        public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
	            final TextView text = (TextView) liparam.view;
	            text.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						if(Resources.getSystem().getString(android.R.string.paste).equals(text.getText().toString())){
							actionBar.open();
							return true;}
						else {
							Toast.makeText(ctx, "Clicked "+text.getText().toString(), Toast.LENGTH_SHORT).show();
							return false;			
						}
						
					}
				});
	            
	        }
	    });
	}
	
	//---------------------------------------------------------------------------------------------------//
	//-------------------------------------------BROWESR-------------------------------------------------//
	//---------------------------------------------------------------------------------------------------//
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.chrome.beta") || lpparam.packageName.equals("com.android.chrome")){
            
	
        XposedHelpers.findAndHookMethod("org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader, "onCreateActionMode",ActionMode.class,Menu.class,  new XC_MethodHook() {
            @Override	
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            	menu =(Menu) param.args[1];
            	menu.add(android.view.Menu.NONE, id,android.view.Menu.NONE, "Clip Board");
    			menu.findItem(id).setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        });
        XposedHelpers.findAndHookMethod("org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader, "onActionItemClicked",ActionMode.class,MenuItem.class,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
            	Object[] args ={};
            	ctx=(Context) XposedHelpers.findMethodBestMatch(
               		 XposedHelpers.findClass("org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader), "getContext").invoke(param.thisObject, args);
            	MenuItem item =(MenuItem)param.args[1];
            	mparam=param;
            	switch(item.getItemId()) {
		        case id:
		        	Intent intent = new Intent();
		    		intent.setAction("DHM47.Xposed.ClipBoard");
		    		ctx.sendBroadcast(intent);
		    		mparam.args[1]=menu.getItem(3);
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
		                		else{XposedHelpers.callMethod(mparam.thisObject, "onActionItemClicked", mparam.args);}
		    				} catch (Throwable e) {
		    					Toast.makeText(ctx, "could not call(selection)", Toast.LENGTH_SHORT).show();
		    					e.printStackTrace();
		    				}
		            }};
		            mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		    		param.setResult(true);
		    		return;
		    		
				}
		        
            }
        });
        XposedHelpers.findAndHookMethod("org.chromium.content.browser.input.InsertionHandleController.PastePopupMenu", lpparam.classLoader, "onClick",View.class,new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
            			View mview =(View) param.args[0];
            			ctx=mview.getContext();
            			Intent intent = new Intent();
			    		intent.setAction("DHM47.Xposed.ClipBoard");
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
			                		else{XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);}
			    				} catch (Throwable e) {
			    					Toast.makeText(ctx, "could not call(click)", Toast.LENGTH_SHORT).show();
			    					e.printStackTrace();
			    				}
			            }};
			            mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			            param.setResult(null);
						return ;
						
					}
				});
        }
    }
	
	
	//---------------------------------------------------------------------------------------------------//
	//-------------------------------------------TESTING-------------------------------------------------//
	//---------------------------------------------------------------------------------------------------//.
	/*XposedHelpers.findAndHookMethod("android.widget.Editor.ActionPopupWindow", null,"onClick", View.class, new XC_MethodHook(){
	@Override
    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
		try {
			TextView text = (TextView) param.args[0];
			if(Resources.getSystem().getString(android.R.string.paste).equals(text.getText().toString())){
				actionBar.open();
				param.setResult(null);
				return ;}
			else{Toast.makeText(ctx, "not paste", Toast.LENGTH_SHORT).show();}
		} catch (Exception e) {
			Toast.makeText(ctx, "Not textview", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		
		}
	});*/
	    //------------------------------------------------------------
	    /*if(resparam.packageName.equals("com.chrome.beta")){
	    XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
	    resparam.res.setReplacement("com.chrome.beta", "menu", "select_action_menu", modRes.fwd(com.dhm47.nativeclipboard.R.menu.menu));}*/
	    //------------------------------------------------------------
	    /*resparam.res.hookLayout("android", "layout", "text_edit_paste_window", new XC_LayoutInflated() {
	        @Override
	        public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
	            LinearLayout layout = (LinearLayout) liparam.view;
	            final TextView text = (TextView)layout.findViewById(android.R.id.title);
	            text.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						//if(Resources.getSystem().getString(android.R.string.paste).equals(text.getText().toString())){
							actionBar.open();
							return true;//}	else return false;			
						
					}
				});
	            
	        }
	    });*/
	
}
