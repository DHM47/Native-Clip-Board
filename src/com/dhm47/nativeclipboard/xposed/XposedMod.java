package com.dhm47.nativeclipboard.xposed;



import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class XposedMod implements IXposedHookZygoteInit,IXposedHookLoadPackage ,IXposedHookInitPackageResources{
	//Clip Board
	private  Context CBMctx;
	private  String pkg;
	//Edit Texts
	private  static Context Ectx;
	private  TextView Etextview;
	private boolean shouldWindowFocusWait;
	//Chrome
	private  Context CSctx;
	private  Context CPctx;
	//FireFox	
	private int FFindex;
	//HTC
	private  static TextView htcTextView;
	private static Object htcObject;
	private static Drawable htcDrawable;
	private boolean htcCBadded=false;
	//Common
	static Menu menu;
	final int id=1259;
	static MethodHookParam mparam;
	private ClipboardManager mClipboardManager;	
	private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
	XSharedPreferences pref;
	private static String clipboard="Clip Board";
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		pref=new XSharedPreferences("com.dhm47.nativeclipboard","com.dhm47.nativeclipboard_preferences");
				
		if(!(pref.getBoolean("monitorservice", false))){
		XposedHelpers.findAndHookConstructor(ClipboardManager.class,Context.class,Handler.class, new XC_MethodHook(){
			@Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				CBMctx=(Context) param.args[0];
				pkg=CBMctx.getPackageName();
			}
		});
		XposedHelpers.findAndHookMethod(ClipboardManager.class, "setPrimaryClip", ClipData.class, new XC_MethodHook(){
			@Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				ClipData clip=(ClipData) param.args[0];
				if(!(pkg.equals("com.dhm47.nativeclipboard"))){
					SendClip(clip.getItemAt(0).coerceToText(CBMctx).toString());
				}
			}
		});
		XposedHelpers.findAndHookMethod(ClipboardManager.class, "setText", CharSequence.class, new XC_MethodHook(){
			@Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				CharSequence clip=(CharSequence) param.args[0];
				if(!(pkg.equals("com.dhm47.nativeclipboard"))){
					SendClip(clip.toString());
				}
			}
		});
		}			
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
							if(pref.getBoolean("pastefunction", false))
								Etextview.onTextContextMenuItem(android.R.id.paste);
							else {
								Open(text.getContext(),Etextview);
				    			WaitforClip(text.getContext(), Etextview);
				    			}
							return true;
						}else {
							Toast.makeText(text.getContext(), "Long Clicked "+text.getText().toString(), Toast.LENGTH_SHORT).show();
							return false;			
						}
					}
				});
	        }
	    });
	}
	
	
	@SuppressLint("DefaultLocale")
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if(lpparam.packageName.equals("org.mozilla.firefox") || lpparam.packageName.equals("org.mozilla.firefox_beta") ){

		Class<?> TextSelectionActionModeCallback=XposedHelpers.findClass("org.mozilla.gecko.TextSelection.TextSelectionActionModeCallback", lpparam.classLoader);
			
		XposedBridge.hookAllMethods(TextSelectionActionModeCallback, "onPrepareActionMode$1451e89d",new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	JSONArray array=(JSONArray) XposedHelpers.getObjectField(param.thisObject, "mItems");
            	FFindex=array.length();
            	JSONObject obj = new JSONObject();
            	obj.put("label", "Clip Board");
            	obj.put("showAsAction", false);
            	array.put(FFindex, obj);
            	menu=(Menu) param.args[0];
            }
        });
		XposedBridge.hookAllMethods(TextSelectionActionModeCallback, "onActionItemClicked$139dd3d0",new XC_MethodHook() {
	        @Override
	        protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
            	final Context ctx=((ImageView)XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "anchorHandle")).getContext();
	    		MenuItem item =(MenuItem)param.args[0];
	    		mparam=param;
	    		if(item.getItemId()==FFindex) {
	        			Open(ctx);
	        			
	        			switch (FFindex) {
						case 2:
							mparam.args[0]=menu.getItem(0);
							break;
						case 3:
							mparam.args[0]=menu.getItem(1);
							break;
						default:
							mparam.args[0]=menu.getItem(3);
							break;
						}

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
	            	            	try {XposedHelpers.callMethod(mparam.thisObject, "onActionItemClicked$139dd3d0", mparam.args);
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
		
		}
		XposedHelpers.findAndHookMethod(TextView.class, "onFocusChanged", boolean.class, int.class,	Rect.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				TextView textView = (TextView) param.thisObject;
				boolean isEditText = textView instanceof EditText;
				if (!isEditText)
					return;
				boolean focused = (Boolean) param.args[0];
				if (focused) {
					Etextview=textView;
					Ectx=Etextview.getContext();
				}
			}
		});
			
		if(Build.VERSION.SDK_INT>15){
		if(pref.getBoolean("cbbutton", true)){//Should The CB button be added?
			XposedHelpers.findAndHookMethod("android.widget.Editor.SelectionActionModeCallback", lpparam.classLoader, "onCreateActionMode",ActionMode.class,Menu.class,OnCreateHook);
	    	XposedHelpers.findAndHookMethod("android.widget.Editor.SelectionActionModeCallback", lpparam.classLoader, "onActionItemClicked",ActionMode.class,MenuItem.class,onItemClickedHook);
			
    	//Fix the overflow bug
    	XposedHelpers.findAndHookMethod("android.widget.Editor", lpparam.classLoader, "onWindowFocusChanged",boolean.class, new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	if(shouldWindowFocusWait) {
            		param.setResult(null);
            		return;
            	    }
            }
        });
    	XposedHelpers.findAndHookMethod("android.widget.Editor.SelectionActionModeCallback", lpparam.classLoader, "onPrepareActionMode",ActionMode.class,Menu.class,  new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	shouldWindowFocusWait=true;
            }
        });
    	XposedHelpers.findAndHookMethod("android.widget.Editor.SelectionActionModeCallback", lpparam.classLoader, "onDestroyActionMode",ActionMode.class,  new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	shouldWindowFocusWait=false;
            }
        });
    	}
		
    	if(pref.getBoolean("pastefunction", false)){//Single click on paste
    		XposedHelpers.findAndHookMethod("android.widget.Editor.ActionPopupWindow", lpparam.classLoader, "onClick",View.class,PasteHook);
    	}
		}else{//IceCreamSandwich Support
		if(pref.getBoolean("cbbutton", true)){//Should The CB button be added?
			XposedHelpers.findAndHookMethod("android.widget.TextView.SelectionActionModeCallback", lpparam.classLoader, "onCreateActionMode",ActionMode.class,Menu.class,OnCreateHook);
	    	XposedHelpers.findAndHookMethod("android.widget.TextView.SelectionActionModeCallback", lpparam.classLoader, "onActionItemClicked",ActionMode.class,MenuItem.class,onItemClickedHook);
		}
	    if(pref.getBoolean("pastefunction", false)){//Single click on paste
	    	XposedHelpers.findAndHookMethod("android.widget.TextView.ActionPopupWindow", lpparam.classLoader, "onClick",View.class,PasteHook);
	    }
		}
		
		//---------------------------------------------------------------------------------------------------//
    	//-----------------------------------------HTC Support-----------------------------------------------//
    	//---------------------------------------------------------------------------------------------------//
		if (Build.MANUFACTURER.toLowerCase().contains("htc")) {
					XposedBridge.hookAllMethods(XposedHelpers.findClass("com.htc.textselection.HtcTextSelectionManager",lpparam.classLoader), "showQuickAction", new XC_MethodHook() {
			            
						@Override	
			            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							TextView mTextView =(TextView) param.args[0];
			            	htcTextView=mTextView;
			            }
					});
					
					XposedHelpers.findAndHookMethod("com.htc.quickselection.HtcQuickSelectionWindow", lpparam.classLoader, "addButton", Object.class,Drawable.class,View.OnClickListener.class,String.class,new XC_MethodHook() {
					    
						@Override	
					    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							String mString=(String)param.args[3];

							if(Resources.getSystem().getString(android.R.string.paste).equals(mString) && !htcCBadded){
								htcCBadded=true;
								final Context htcctx=htcTextView.getContext();
								htcObject=param.args[0];
								htcDrawable=(Drawable) param.args[1];
								View.OnClickListener mClick=new OnClickListener() {
									@Override
									public void onClick(View v) {
										Open(htcctx,htcTextView);
					        			WaitforClip(htcctx, htcTextView);
									}
								};
								Object[] args ={htcObject,htcDrawable,mClick,clipboard};
								try {
									XposedBridge.invokeOriginalMethod(param.method, param.thisObject, args);
								} catch (Exception e) {
									XposedBridge.log(e);
								}
							}else if(Resources.getSystem().getString(android.R.string.paste).equals(mString)){
								htcCBadded=false;
							}
					    }
					});
			}
    	//---------------------------------------------------------------------------------------------------//
    	//-------------------------------------------BROWESR-------------------------------------------------//
    	//---------------------------------------------------------------------------------------------------//
		if (lpparam.packageName.equals("com.google.android.webview") || lpparam.packageName.equals("com.android.webview")){
			XposedHelpers.findAndHookMethod("com.android.org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader, "onCreateActionMode",ActionMode.class,Menu.class,  new XC_MethodHook() {
	            @Override	
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	menu =(Menu) param.args[1];
	            	CBButton(menu);
	            }
	        });
			XposedHelpers.findAndHookMethod("com.android.org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader, "onActionItemClicked",ActionMode.class,MenuItem.class,  new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
	            	Object[] args ={};
	            	CSctx=(Context) XposedHelpers.findMethodBestMatch(
	               		 XposedHelpers.findClass("com.android.org.chromium.content.browser.SelectActionModeCallback", lpparam.classLoader), "getContext").invoke(param.thisObject, args);
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
			                	if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(CSctx).toString().equals("//NATIVECLIPBOARDCLOSE//")){
		    	            		try {
			    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			    					} catch (Exception e1) {
			    						Toast.makeText(CSctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
			    						e1.printStackTrace();
			    					}	
		    	            	}else if(pref.getBoolean("singlepaste", false)){
	            	            	try {
			    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			    					} catch (Exception e1) {
			    						Toast.makeText(CSctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
			    						e1.printStackTrace();
			    					}	
	            	            	try {XposedHelpers.callMethod(mparam.thisObject, "onActionItemClicked", mparam.args);
		    	            			} catch (Throwable e) {
		    	            				Toast.makeText(CSctx, "could not call(selection)", Toast.LENGTH_SHORT).show();
				    					e.printStackTrace();
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
			XposedHelpers.findAndHookMethod("com.android.org.chromium.content.browser.input.PastePopupMenu", lpparam.classLoader, "onClick",View.class,new XC_MethodHook() {
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
				    						Toast.makeText(CPctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
				    						e1.printStackTrace();
				    					}	
			    	            	}else if(pref.getBoolean("singlepaste", false)){
		            	            	try {
				    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
				    					} catch (Exception e1) {
				    						Toast.makeText(CPctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
				    						e1.printStackTrace();
				    					}	

		    	            			try {XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
		    	            			} catch (Throwable e) {
				    					Toast.makeText(CPctx, "could not call(click)", Toast.LENGTH_SHORT).show();
				    					e.printStackTrace();
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
		
		if (lpparam.packageName.equals("com.chrome.beta") || lpparam.packageName.equals("com.android.chrome")){
            
		if(pref.getBoolean("cbbutton", true)){
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
		                	if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(CSctx).toString().equals("//NATIVECLIPBOARDCLOSE//")){
	    	            		try {
		    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		    					} catch (Exception e1) {
		    						Toast.makeText(CSctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
		    						e1.printStackTrace();
		    					}	
	    	            	}else if(pref.getBoolean("singlepaste", false)){
            	            	try {
		    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		    					} catch (Exception e1) {
		    						Toast.makeText(CSctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
		    						e1.printStackTrace();
		    					}	
            	            	try {XposedHelpers.callMethod(mparam.thisObject, "onActionItemClicked", mparam.args);
	    	            			} catch (Throwable e) {
	    	            				Toast.makeText(CSctx, "could not call(selection)", Toast.LENGTH_SHORT).show();
			    					e.printStackTrace();
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
        }
        
		XposedHelpers.findAndHookMethod("org.chromium.content.browser.input.PastePopupMenu", lpparam.classLoader, "onClick",View.class,new XC_MethodHook() {
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
			    						Toast.makeText(CPctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
			    						e1.printStackTrace();
			    					}	
		    	            	}else if(pref.getBoolean("singlepaste", false)){
	            	            	try {
			    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			    					} catch (Exception e1) {
			    						Toast.makeText(CPctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
			    						e1.printStackTrace();
			    					}	

	    	            			try {XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
	    	            			} catch (Throwable e) {
			    					Toast.makeText(CPctx, "could not call(click)", Toast.LENGTH_SHORT).show();
			    					e.printStackTrace();
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
	
	private void CBButton(Menu menu2){
		menu2.add(android.view.Menu.NONE, id,android.view.Menu.NONE, clipboard);
		menu2.findItem(id).setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	
	private void Open(Context mctx) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.dhm47.nativeclipboard","com.dhm47.nativeclipboard.ClipBoard"));
		mctx.startActivity(intent);
	}
	
	private void Open(Context mctx , TextView mTextview) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.dhm47.nativeclipboard","com.dhm47.nativeclipboard.ClipBoard"));
		int[] location = new int[2];
		Rect r = new Rect();
		mTextview.getLocationOnScreen(location);
		mTextview.getWindowVisibleDisplayFrame(r);
		double Precentage =((double)location[1])/(r.bottom-r.top);
		intent.putExtra("Keyheight",Precentage);
		mctx.startActivity(intent);
	}
	
	private void SendClip(String clip){
		Intent intent = new Intent();
		intent.setAction("DHM47.Xposed.ClipBoardMonitor");
		intent.putExtra("Package", pkg);
		intent.putExtra("Clip",clip);
		intent.putExtra("Time", System.currentTimeMillis());
		CBMctx.sendBroadcast(intent);
	}
	
	private void WaitforClip(final Context ctx,final TextView mTextview){
		
		mClipboardManager =(ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
		mOnPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
            	String primaryClip=mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(ctx).toString();

            	if(primaryClip.equals("//NATIVECLIPBOARDCLOSE//")){
            		try {
						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
					} catch (Exception e1) {
						Toast.makeText(ctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
						e1.printStackTrace();
					}	
            		final Handler handler = new Handler();
            		handler.postDelayed(new Runnable() {
            		    @Override
            		    public void run() {
            		    	InputMethodManager inputMethodManager=(InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
    	            	    inputMethodManager.showSoftInput(mTextview, InputMethodManager.SHOW_IMPLICIT);
            		    }
            		}, 300);
            	}else if(pref.getBoolean("singlepaste", false)){
	            	try {
						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
					} catch (Exception e1) {
						Toast.makeText(ctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
						e1.printStackTrace();
					}
	            	try {   mTextview.onTextContextMenuItem(android.R.id.paste);
	            	} catch (Throwable e) {
	            		Toast.makeText(ctx, "pasting went wrong", Toast.LENGTH_SHORT).show();
	            		e.printStackTrace();
	            	}
	            }
        		else{
        			try {   mTextview.onTextContextMenuItem(android.R.id.paste);
        			} catch (Throwable e) {
					Toast.makeText(ctx, "pasting went wrong", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
        			}		    	            	
            				    	            	
        }};
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
	}
	
	XC_MethodHook PasteHook=new XC_MethodHook() {
        @Override	
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        	TextView text =(TextView) param.args[0];
        	if(Resources.getSystem().getString(android.R.string.paste).equals(text.getText().toString())){
        		Open(Ectx,Etextview);
				WaitforClip(Ectx, Etextview);
            	param.setResult(null);
				return ;
			}else {
				Toast.makeText(Ectx, "NCB click",Toast.LENGTH_SHORT).show();
			}
        }
    };
	
    XC_MethodHook onItemClickedHook =new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
        	MenuItem item =(MenuItem)param.args[1];
        	switch(item.getItemId()) {
        		case id:
        			Open(Ectx,Etextview);
        			WaitforClip(Ectx, Etextview);
        			param.setResult(true);
    				return;
    		}
        }
    };
    
    XC_MethodHook OnCreateHook=new XC_MethodHook() {
        @Override	
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        	menu =(Menu) param.args[1];
        	CBButton(menu);
        }
    };
	
}
