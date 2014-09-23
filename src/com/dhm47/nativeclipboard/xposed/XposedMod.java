package com.dhm47.nativeclipboard.xposed;




import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Selection;
import android.text.Spannable;
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


public class XposedMod implements IXposedHookZygoteInit,IXposedHookLoadPackage ,IXposedHookInitPackageResources{
	//public static Context ctx;
	private  Context CBMctx;
	private  String pkg;
	
	private  Context Ectx;
	private  TextView Etextview;
	
	private  Context CSctx;
	private  Context CPctx;
	//static mActionBar actionBar;
	//static String MODULE_PATH;
	static Menu menu;
	final int id=1259;
	static MethodHookParam mparam;
	private ClipboardManager mClipboardManager;	
	private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		//MODULE_PATH =startupParam.modulePath;
		
		XposedHelpers.findAndHookConstructor(ClipboardManager.class,Context.class,Handler.class, new XC_MethodHook(){
			@Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				CBMctx=(Context) param.args[0];
				Log.d("NativeClipBoard", "got context");
				pkg=CBMctx.getPackageName();
				Log.d("NativeClipBoard", "got context from "+pkg);
			}
		});
		XposedHelpers.findAndHookMethod(ClipboardManager.class, "setPrimaryClip", ClipData.class, new XC_MethodHook(){
			@Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				ClipData clip=(ClipData) param.args[0];
				Log.d("NativeClipBoard", pkg+" copied");
				if(!(pkg.equals("com.dhm47.nativeclipboard"))){
				Intent intent = new Intent();
				intent.setAction("DHM47.Xposed.ClipBoardMonitor");
				intent.putExtra("Package", pkg);
				intent.putExtra("Clip",clip.getItemAt(0).coerceToText(CBMctx).toString());
				CBMctx.sendBroadcast(intent);}
			}
		});
		XposedHelpers.findAndHookMethod(ClipboardManager.class, "setText", CharSequence.class, new XC_MethodHook(){
			@Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				CharSequence clip=(CharSequence) param.args[0];
				Log.d("NativeClipBoard", pkg+" copied(old)");
				if(!(pkg.equals("com.dhm47.nativeclipboard"))){
				Intent intent = new Intent();
				intent.setAction("DHM47.Xposed.ClipBoardMonitor");
				intent.putExtra("Package", pkg);
				intent.putExtra("Clip",clip.toString());
				CBMctx.sendBroadcast(intent);}
			}
		});
		/*XposedHelpers.findAndHookMethod(ClipboardManager.class, "reportPrimaryClipChanged", new XC_MethodHook() {
			@Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				mClipboardManager =(ClipboardManager) CBMctx.getSystemService(Context.CLIPBOARD_SERVICE);
				Log.d("NativeClipBoard", pkg+" copied with listener");
				if(!(pkg.equals("com.dhm47.nativeclipboard"))){
				Intent intent = new Intent();
				intent.setAction("DHM47.Xposed.ClipBoardMonitor");
				intent.putExtra("Package", pkg);
				intent.putExtra("Clip",mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(CBMctx).toString());
				CBMctx.sendBroadcast(intent);}
			}
		});*/
				
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
							Open(text.getContext());
							final int start=Etextview.getSelectionStart();
			    			final int end=Etextview.getSelectionEnd();
			    			mClipboardManager =(ClipboardManager) Ectx.getSystemService(Context.CLIPBOARD_SERVICE);
			    			mOnPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener() {
			    	            @Override
			    	            public void onPrimaryClipChanged() {
			    	            	if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString().equals("//NATIVECLIPBOARDCLOSE//")){
			    	            		try {
				    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
				    					} catch (Exception e1) {
				    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
				    						e1.printStackTrace();
				    					}	
			    	            	}
		    	            		else{
		    	            			try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
			    	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
			    	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
			    	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
			    					} catch (Throwable e) {
			    						Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
			    						e.printStackTrace();
			    					}
		    	            			}		    	            	
			    	            				    	            	
			    	        }};
			    	        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
							return true;}
						else {
							Toast.makeText(text.getContext(), "Long Clicked "+text.getText().toString(), Toast.LENGTH_SHORT).show();
							return false;			
						}
						
					}
				});
	            
	        }
	    });
	}
	
	
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		
		XposedHelpers.findAndHookMethod(TextView.class, "onFocusChanged", boolean.class, int.class,	Rect.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				TextView textView = (TextView) param.thisObject;
				boolean isEditText = textView instanceof EditText;
				if (!isEditText)
				return;
				boolean focused = (Boolean) param.args[0];
				if (focused) {
					Etextview=textView;
					Ectx=Etextview.getContext();
					Log.d("NativeClipBoard", "Got focesed textview"); 
				}
				}
				});
		
    	XposedHelpers.findAndHookMethod("android.widget.Editor.SelectionActionModeCallback", lpparam.classLoader, "onCreateActionMode",ActionMode.class,Menu.class,  new XC_MethodHook() {
            @Override	
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            	menu =(Menu) param.args[1];
            	CBButton(menu);
            }
        });
    	
    	XposedHelpers.findAndHookMethod("android.widget.Editor.SelectionActionModeCallback", lpparam.classLoader, "onActionItemClicked",ActionMode.class,MenuItem.class,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
            	MenuItem item =(MenuItem)param.args[1];
            	switch(item.getItemId()) {
            		case id:
            			Open(Ectx);
            			final int start=Etextview.getSelectionStart();
            			final int end=Etextview.getSelectionEnd();
            			mClipboardManager =(ClipboardManager) Ectx.getSystemService(Context.CLIPBOARD_SERVICE);
            			mOnPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener() {
            	            @Override
            	            public void onPrimaryClipChanged() {
		    	            	if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString().equals("//NATIVECLIPBOARDCLOSE//")){
		    	            		try {
			    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			    					} catch (Exception e1) {
			    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
			    						e1.printStackTrace();
			    					}	
		    	            	}
	    	            		else{
	    	            			try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
		    	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
		    	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
		    	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
		    					} catch (Throwable e) {
		    						Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
		    						e.printStackTrace();
		    					}
	    	            			}		    	            	
		    	            				    	            	
		    	        }};
            	        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
            	        param.setResult(true);
        				return;
        				}
		        
            }
        });
    	/*XposedHelpers.findAndHookMethod("android.widget.Editor.ActionPopupWindow", lpparam.classLoader, "onClick",View.class,  new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	TextView text =(TextView) param.args[0];
            	if(Resources.getSystem().getString(android.R.string.paste).equals(text.getText().toString())){
    			Open(Ectx);
    			final int start=Etextview.getSelectionStart();
    			final int end=Etextview.getSelectionEnd();
    			mClipboardManager =(ClipboardManager) Ectx.getSystemService(Context.CLIPBOARD_SERVICE);
    			mOnPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener() {
    	            @Override
    	            public void onPrimaryClipChanged() {
    	            	try {
    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    					} catch (Exception e1) {
    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
    						e1.printStackTrace();
    					}
    	            	try {
    	            		if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString().equals(""));
    	            		else{   		   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
    	            						  +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
    	            						  +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
    	            		Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());}
    					} catch (Throwable e) {
    						Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
    						e.printStackTrace();
    					}
    	        }};
    	        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
	            param.setResult(null);
				return ;}
            }
        });*/
    	//---------------------------------------------------------------------------------------------------//
    	//-------------------------------------------BROWESR-------------------------------------------------//
    	//---------------------------------------------------------------------------------------------------//
		if (lpparam.packageName.equals("com.chrome.beta") || lpparam.packageName.equals("com.android.chrome")){
            
	
        XposedHelpers.findAndHookMethod("org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader, "onCreateActionMode",ActionMode.class,Menu.class,  new XC_MethodHook() {
            @Override	
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            	menu =(Menu) param.args[1];
            	CBButton(menu);
            }
        });
        XposedHelpers.findAndHookMethod("org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader, "onActionItemClicked",ActionMode.class,MenuItem.class,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
            	Object[] args ={};
            	CSctx=(Context) XposedHelpers.findMethodBestMatch(
               		 XposedHelpers.findClass("org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader), "getContext").invoke(param.thisObject, args);
            	MenuItem item =(MenuItem)param.args[1];
            	mparam=param;
            	switch(item.getItemId()) {
		        case id:
		        	Open(CSctx);
		    		mparam.args[1]=menu.getItem(3);
		    		mClipboardManager =(ClipboardManager) CSctx.getSystemService(Context.CLIPBOARD_SERVICE);
		    		mOnPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener() {
		                @Override
		                public void onPrimaryClipChanged() {
		                	if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(CPctx).toString().equals("//NATIVECLIPBOARDCLOSE//")){
	    	            		try {
		    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		    					} catch (Exception e1) {
		    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
		    						e1.printStackTrace();
		    					}	
	    	            	}
    	            		else{
    	            			try {XposedHelpers.callMethod(mparam.thisObject, "onActionItemClicked", mparam.args);
    	            			} catch (Throwable e) {
    	            				Toast.makeText(CSctx, "could not call(selection)", Toast.LENGTH_SHORT).show();
		    					e.printStackTrace();
		    				}}		    	            	
	    	        
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
            			CPctx=mview.getContext();
            			Open(CPctx);
			    		mClipboardManager =(ClipboardManager) CPctx.getSystemService(Context.CLIPBOARD_SERVICE);
			    		mOnPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener() {
			                @Override
			                public void onPrimaryClipChanged() {
			                	if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(CPctx).toString().equals("//NATIVECLIPBOARDCLOSE//")){
		    	            		try {
			    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			    					} catch (Exception e1) {
			    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
			    						e1.printStackTrace();
			    					}	
		    	            	}
	    	            		else{
	    	            			try {XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
	    	            			} catch (Throwable e) {
			    					Toast.makeText(CPctx, "could not call(click)", Toast.LENGTH_SHORT).show();
			    					e.printStackTrace();
			    				}}		    	            	
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
	private void CBButton(Menu menu2){
		menu2.add(android.view.Menu.NONE, id,android.view.Menu.NONE, "CB");
		menu2.findItem(id).setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	
	private void Open(Context mctx) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.dhm47.nativeclipboard","com.dhm47.nativeclipboard.ClipBoard"));
		mctx.startActivity(intent);
	}
	
	
	
		
		
	
}
