package com.dhm47.nativeclipboard.xposed;





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
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

	private  Context CBMctx;
	private  String pkg;
	
	private  static Context Ectx;
	private  TextView Etextview;
	private int start;
	private int end;
	//private boolean WindowFocus;
	
	private  Context CSctx;
	private  Context CPctx;
		
	private  static TextView htcTextView;
	private static Object htcObject;
	private static Drawable htcDrawable;
	private boolean htcCBadded=false;
	
	static Menu menu;
	final int id=1259;
	static MethodHookParam mparam;
	private ClipboardManager mClipboardManager;	
	private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
	XSharedPreferences pref;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		pref=new XSharedPreferences("com.dhm47.nativeclipboard","com.dhm47.nativeclipboard_preferences");
		if(!(pref.getBoolean("monitorservice", false))){
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
				intent.putExtra("Time", System.currentTimeMillis());
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
				intent.putExtra("Time", System.currentTimeMillis());
				CBMctx.sendBroadcast(intent);}
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
							if(pref.getBoolean("pastefunction", false))Etextview.onTextContextMenuItem(android.R.id.paste);
							else {
								Open(text.getContext());
								start=Etextview.getSelectionStart();
				    			end=Etextview.getSelectionEnd();
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
				    	            		final Handler handler = new Handler();
				    	            		handler.postDelayed(new Runnable() {
				    	            		    @Override
				    	            		    public void run() {
				    	            		    	InputMethodManager inputMethodManager=(InputMethodManager)Ectx.getSystemService(Context.INPUT_METHOD_SERVICE);
						    	            	    inputMethodManager.showSoftInput(Etextview, InputMethodManager.SHOW_IMPLICIT);
				    	            		    }
				    	            		}, 300);
				    	            	}else if(pref.getBoolean("singlepaste", false)){
			            	            	try {
					    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
					    					} catch (Exception e1) {
					    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
					    						e1.printStackTrace();
					    					}
			            	            	try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
			   	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
			   	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
			   	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
			            	            	} catch (Throwable e) {
			            	            		Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
			            	            		e.printStackTrace();
			            	            	}
			            	            	
			            	            }
			    	            		else{
			    	            			try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
				    	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
				    	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
				    	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
					   	            				start=start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length();
					   	            				end=start;
			    	            			} catch (Throwable e) {
				    						Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
				    						e.printStackTrace();
				    					}
			    	            			}		    	            	
				    	            				    	            	
				    	        }};
				    	        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
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
		
		XposedHelpers.findAndHookMethod(TextView.class, "onFocusChanged", boolean.class, int.class,	Rect.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				//XposedBridge.log("Hooked onFocusChanged");
			TextView textView = (TextView) param.thisObject;
			boolean isEditText = textView instanceof EditText;
			if (!isEditText)
			return;
			boolean focused = (Boolean) param.args[0];
			if (focused) {
				Etextview=textView;
				Ectx=Etextview.getContext();
				//Log.d("NativeClipBoard", "Got focesed textview"); 
			}
			}
			});
	
		
		//Trying to make it work on HTC Sense
		if (Build.MANUFACTURER.toLowerCase().contains("htc")) {
			XposedBridge.hookAllMethods(XposedHelpers.findClass("com.htc.textselection.HtcTextSelectionManager",lpparam.classLoader), "showQuickAction", new XC_MethodHook() {
	            
				@Override	
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					TextView mTextView =(TextView) param.args[0];
	            	//XposedBridge.log("Hooked showQuickAction");
	            	htcTextView=mTextView;
	            }
			});
			
			XposedHelpers.findAndHookMethod("com.htc.quickselection.HtcQuickSelectionWindow", lpparam.classLoader, "addButton", Object.class,Drawable.class,View.OnClickListener.class,String.class,new XC_MethodHook() {
			    
				@Override	
			    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					//XposedBridge.log("Hooked addButton");
					String mString=(String)param.args[3];
					//XposedBridge.log("added"+mString);

					if(Resources.getSystem().getString(android.R.string.paste).equals(mString) && !htcCBadded){
						//XposedBridge.log("added"+"ClipBoard");
						htcCBadded=true;
						final Context htcctx=htcTextView.getContext();
						//Toast.makeText(htcctx, "Hooked 'addButton' ", Toast.LENGTH_LONG).show();
						htcObject=param.args[0];
						htcDrawable=(Drawable) param.args[1];
						View.OnClickListener mClick=new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Open(htcctx);
								start=htcTextView.getSelectionStart();
			        			end=htcTextView.getSelectionEnd();
			        			mClipboardManager =(ClipboardManager) htcctx.getSystemService(Context.CLIPBOARD_SERVICE);
			        			mOnPrimaryClipChangedListener =new ClipboardManager.OnPrimaryClipChangedListener() {
			        	            @Override
			        	            public void onPrimaryClipChanged() {
			        	            if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(htcctx).toString().equals("//NATIVECLIPBOARDCLOSE//")){
				    	            		try {
					    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
					    					} catch (Exception e1) {
					    						Toast.makeText(htcctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
					    						e1.printStackTrace();
					    					}	
				    	            		final Handler handler = new Handler();
				    	            		handler.postDelayed(new Runnable() {
				    	            		    @Override
				    	            		    public void run() {
				    	            		    	InputMethodManager inputMethodManager=(InputMethodManager)htcctx.getSystemService(Context.INPUT_METHOD_SERVICE);
						    	            	    inputMethodManager.showSoftInput(htcTextView, InputMethodManager.SHOW_IMPLICIT);
				    	            		    }
				    	            		}, 300);
				    	            		
				    	            	}else if(pref.getBoolean("singlepaste", false)){
			            	            	try {
					    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
					    					} catch (Exception e1) {
					    						Toast.makeText(htcctx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
					    						e1.printStackTrace();
					    					}
			            	            	try {   htcTextView.setText(htcTextView.getText().subSequence(0, start).toString()
			   	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(htcctx).toString()
			   	            					 +htcTextView.getText().subSequence(end, htcTextView.getText().length()).toString());
			   	            				Selection.setSelection((Spannable) htcTextView.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(htcctx).length());
			            	            	} catch (Throwable e) {
			            	            		Toast.makeText(htcctx, "pasting went wrong", Toast.LENGTH_SHORT).show();
			            	            		e.printStackTrace();
			            	            	}
			            	            	
			            	            }
			    	            		else{
			    	            			try {   htcTextView.setText(htcTextView.getText().subSequence(0, start).toString()
				    	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(htcctx).toString()
				    	            					 +htcTextView.getText().subSequence(end, htcTextView.getText().length()).toString());
				    	            				Selection.setSelection((Spannable) htcTextView.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(htcctx).length());
					   	            				start=start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(htcctx).length();
					   	            				end=start;
			    	            			} catch (Throwable e) {
				    						Toast.makeText(htcctx, "pasting went wrong", Toast.LENGTH_SHORT).show();
				    						e.printStackTrace();
				    					}
			    	            			}		    	            	
				    	            				    	            	
				    	        }};
			        	        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			        	
								
							}
						};
						Object[] args ={htcObject,htcDrawable,mClick,"ClipBoard"};
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
		/*XposedBridge.hookAllMethods(XposedHelpers.findClass("com.htc.textselection.HtcTextSelectionManager",lpparam.classLoader), "prepareQuickActions", new XC_MethodHook() {
	            
				@Override	
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					final View mView =(View) param.args[0];
	            	//Toast.makeText(mView.getContext(),"Successfully hooked 'prepareQuickActions'",Toast.LENGTH_LONG).show();
					Drawable paste=(Drawable)XposedHelpers.getObjectField(param.thisObject, "icon_paste");
					Object mSelectionWindow=XposedHelpers.getObjectField(param.thisObject, "mSelectionWindow");
					OnClickListener mClick=new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Open(mView.getContext());
							
						}
					};
					String mString="clipboard";
					Object[] args ={null,paste,mClick,mString};
					
					XposedHelpers.callMethod(mSelectionWindow,"addButton", args);
					XposedBridge.log("Hooked prepareQuickActions");
	            }
			});
		/*XposedBridge.hookAllMethods(XposedHelpers.findClass("com.htc.textselection.HtcTextSelectionManager",lpparam.classLoader), "showQuickAction", new XC_MethodHook() {
            
			@Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				TextView mTextView =(TextView) param.args[0];
            	Toast.makeText(mTextView.getContext(),"Successfully hooked 'showQuickAction'",Toast.LENGTH_LONG).show();
            	XposedBridge.log("Hooked showQuickAction");
            }
		});
		
			/*
		XposedHelpers.findAndHookMethod("com.htc.quickselection.HtcQuickSelectionWindow", lpparam.classLoader, "onItemClick",AdapterView.class,View.class,int.class,long.class,  new XC_MethodHook() {
            @Override	
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            	View mView =(View) param.args[1];
            	Toast.makeText(mView.getContext(),"Item click in HtcQuickSelectionWindow",Toast.LENGTH_LONG).show();
            	XposedBridge.log("Item click in HtcQuickSelectionWindow");
            }
		});
		XposedHelpers.findAndHookMethod("com.htc.quickselection.QuickSelectionWindow", lpparam.classLoader, "onItemClick",AdapterView.class,View.class,int.class,long.class,  new XC_MethodHook() {
            @Override	
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            	View mView =(View) param.args[1];
            	Toast.makeText(mView.getContext(),"Item click in QuickSelectionWindow",Toast.LENGTH_LONG).show();
            	XposedBridge.log("Item click in QuickSelectionWindow");
            }
		});
		try {
			XposedHelpers.findAndHookMethod(
					"com.htc.textselection.HtcTextSelectionManager",
					lpparam.classLoader, "prepareQuickActions", View.class,
					Menu.class, OnClickListener.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {
							XposedBridge.log("Hook in prepareQuickActions");
						}
					});
		} catch (Exception e) {
			XposedBridge.log("Faild to hook prepareQuickActions");
			//XposedBridge.log(e);
		}
		
		try {
			XposedHelpers.findAndHookMethod(
					"com.htc.textselection.HtcTextSelectionManager",
					lpparam.classLoader, "prepareHtcPasteWindow", View.class,
					OnClickListener.class, Context.class, boolean.class,
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {
							XposedBridge.log("Hook in prepareHtcPasteWindow");
						}
					});
		} catch (Exception e) {
			XposedBridge.log("Faild to hook prepareHtcPasteWindow");
			//XposedBridge.log(e);
		}
		
		try {
			XposedHelpers.findAndHookMethod(
					"com.htc.textselection.HtcTextSelectionManager",
					lpparam.classLoader, "prepareQuickActions", View.class,
					OnClickListener.class, Context.class, boolean.class,
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {
							XposedBridge.log("Hook in prepareQuickActions2");
						}
					});
		} catch (Exception e) {
			XposedBridge.log("Faild to hook prepareQuickActions2");
			//XposedBridge.log(e);
		}
		*/
		
		if(Build.VERSION.SDK_INT>15){
		if(pref.getBoolean("cbbutton", true)){
    	XposedHelpers.findAndHookMethod("android.widget.Editor.SelectionActionModeCallback", lpparam.classLoader, "onCreateActionMode",ActionMode.class,Menu.class,  new XC_MethodHook() {
            @Override	
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            	menu =(Menu) param.args[1];
            	CBButton(menu);
            }
        });
    	/*Attempt to fix the overflow bug
    	XposedHelpers.findAndHookMethod("android.widget.Editor", lpparam.classLoader, "onWindowFocusChanged",boolean.class, new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	WindowFocus = (Boolean) param.args[0];
            }
        });
    	XposedHelpers.findAndHookMethod("android.widget.Editor", lpparam.classLoader, "hideControllers", new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	if(!WindowFocus){
            		WindowFocus=true;
            		param.setResult(null);
            		return;
            	}
            }
        });*/
    	XposedHelpers.findAndHookMethod("android.widget.Editor.SelectionActionModeCallback", lpparam.classLoader, "onActionItemClicked",ActionMode.class,MenuItem.class,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
            	MenuItem item =(MenuItem)param.args[1];
            	
            	switch(item.getItemId()) {
            		case id:
            			Open(Ectx);
            			start=Etextview.getSelectionStart();
            			end=Etextview.getSelectionEnd();
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
		    	            		final Handler handler = new Handler();
		    	            		handler.postDelayed(new Runnable() {
		    	            		    @Override
		    	            		    public void run() {
		    	            		    	InputMethodManager inputMethodManager=(InputMethodManager)Ectx.getSystemService(Context.INPUT_METHOD_SERVICE);
				    	            	    inputMethodManager.showSoftInput(Etextview, InputMethodManager.SHOW_IMPLICIT);
		    	            		    }
		    	            		}, 300);
		    	            		
		    	            	}else if(pref.getBoolean("singlepaste", false)){
	            	            	try {
			    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			    					} catch (Exception e1) {
			    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
			    						e1.printStackTrace();
			    					}
	            	            	try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
	   	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
	   	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
	   	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
	            	            	} catch (Throwable e) {
	            	            		Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
	            	            		e.printStackTrace();
	            	            	}
	            	            	
	            	            }
	    	            		else{
	    	            			try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
		    	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
		    	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
		    	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
			   	            				start=start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length();
			   	            				end=start;
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
		}
		
    	if(pref.getBoolean("pastefunction", false)){
    	XposedHelpers.findAndHookMethod("android.widget.Editor.ActionPopupWindow", lpparam.classLoader, "onClick",View.class,  new XC_MethodHook() {
            @Override	
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	TextView text =(TextView) param.args[0];
            	if(Resources.getSystem().getString(android.R.string.paste).equals(text.getText().toString())){
    			Open(Ectx);
    			start=Etextview.getSelectionStart();
    			end=Etextview.getSelectionEnd();
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
    	            		final Handler handler = new Handler();
    	            		handler.postDelayed(new Runnable() {
    	            		    @Override
    	            		    public void run() {
    	            		    	InputMethodManager inputMethodManager=(InputMethodManager)Ectx.getSystemService(Context.INPUT_METHOD_SERVICE);
		    	            	    inputMethodManager.showSoftInput(Etextview, InputMethodManager.SHOW_IMPLICIT);
    	            		    }
    	            		}, 300);
    	            	}else if(pref.getBoolean("singlepaste", false)){
        	            	try {
	    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
	    					} catch (Exception e1) {
	    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
	    						e1.printStackTrace();
	    					}
        	            	try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
        	            	} catch (Throwable e) {
        	            		Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
        	            		e.printStackTrace();
        	            	}
        	            	
        	            }
	            		else{
	            			try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
    	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
    	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
    	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
	   	            				start=start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length();
	   	            				end=start;
    					} catch (Throwable e) {
    						Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
    						e.printStackTrace();
    					}
	            			}		    	            	
    	            				    	            	
    	        }};
    	        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
	            param.setResult(null);
				return ;}else {
					Toast.makeText(Ectx, "NCB click",Toast.LENGTH_SHORT).show();
				}
            }
        });
    	}
		}else{
			if(pref.getBoolean("cbbutton", true)){
	    	XposedHelpers.findAndHookMethod("android.widget.TextView.SelectionActionModeCallback", lpparam.classLoader, "onCreateActionMode",ActionMode.class,Menu.class,  new XC_MethodHook() {
	            @Override	
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	menu =(Menu) param.args[1];
	            	CBButton(menu);
	            }
	        });
	    	
	    	XposedHelpers.findAndHookMethod("android.widget.TextView.SelectionActionModeCallback", lpparam.classLoader, "onActionItemClicked",ActionMode.class,MenuItem.class,  new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
	            	MenuItem item =(MenuItem)param.args[1];
	            	switch(item.getItemId()) {
	            		case id:
	            			Open(Ectx);
	            			start=Etextview.getSelectionStart();
	            			end=Etextview.getSelectionEnd();
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
			    	            		final Handler handler = new Handler();
			    	            		handler.postDelayed(new Runnable() {
			    	            		    @Override
			    	            		    public void run() {
			    	            		    	InputMethodManager inputMethodManager=(InputMethodManager)Ectx.getSystemService(Context.INPUT_METHOD_SERVICE);
					    	            	    inputMethodManager.showSoftInput(Etextview, InputMethodManager.SHOW_IMPLICIT);
			    	            		    }
			    	            		}, 300);
			    	            		
			    	            	}else if(pref.getBoolean("singlepaste", false)){
		            	            	try {
				    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
				    					} catch (Exception e1) {
				    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
				    						e1.printStackTrace();
				    					}
		            	            	try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
		   	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
		   	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
		   	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
		            	            	} catch (Throwable e) {
		            	            		Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
		            	            		e.printStackTrace();
		            	            	}
		            	            	
		            	            }
		    	            		else{
		    	            			try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
			    	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
			    	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
			    	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
				   	            				start=start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length();
				   	            				end=start;
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
			}
			
	    	if(pref.getBoolean("pastefunction", false)){
	    	XposedHelpers.findAndHookMethod("android.widget.TextView.ActionPopupWindow", lpparam.classLoader, "onClick",View.class,  new XC_MethodHook() {
	            @Override	
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	TextView text =(TextView) param.args[0];
	            	if(Resources.getSystem().getString(android.R.string.paste).equals(text.getText().toString())){
	    			Open(Ectx);
	    			start=Etextview.getSelectionStart();
	    			end=Etextview.getSelectionEnd();
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
	    	            		final Handler handler = new Handler();
	    	            		handler.postDelayed(new Runnable() {
	    	            		    @Override
	    	            		    public void run() {
	    	            		    	InputMethodManager inputMethodManager=(InputMethodManager)Ectx.getSystemService(Context.INPUT_METHOD_SERVICE);
			    	            	    inputMethodManager.showSoftInput(Etextview, InputMethodManager.SHOW_IMPLICIT);
	    	            		    }
	    	            		}, 300);
	    	            	}else if(pref.getBoolean("singlepaste", false)){
	        	            	try {
		    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		    					} catch (Exception e1) {
		    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
		    						e1.printStackTrace();
		    					}
	        	            	try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
		            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
		            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
		            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
	        	            	} catch (Throwable e) {
	        	            		Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
	        	            		e.printStackTrace();
	        	            	}
	        	            	
	        	            }
		            		else{
		            			try {   Etextview.setText(Etextview.getText().subSequence(0, start).toString()
	    	            					 +mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).toString()
	    	            					 +Etextview.getText().subSequence(end, Etextview.getText().length()).toString());
	    	            				Selection.setSelection((Spannable) Etextview.getText(), start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length());
		   	            				start=start+mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(Ectx).length();
		   	            				end=start;
	    					} catch (Throwable e) {
	    						Toast.makeText(Ectx, "pasting went wrong", Toast.LENGTH_SHORT).show();
	    						e.printStackTrace();
	    					}
		            			}		    	            	
	    	            				    	            	
	    	        }};
	    	        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		            param.setResult(null);
					return ;}else {
						Toast.makeText(Ectx, "NCB click",Toast.LENGTH_SHORT).show();
					}
	            }
	        });
	    	}
		}
    	//---------------------------------------------------------------------------------------------------//
    	//-------------------------------------------BROWESR-------------------------------------------------//
    	//---------------------------------------------------------------------------------------------------//
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
		                	if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(CPctx).toString().equals("//NATIVECLIPBOARDCLOSE//")){
	    	            		try {
		    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		    					} catch (Exception e1) {
		    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
		    						e1.printStackTrace();
		    					}	
	    	            	}else if(pref.getBoolean("singlepaste", false)){
            	            	try {
		    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		    					} catch (Exception e1) {
		    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
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
			    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
			    						e1.printStackTrace();
			    					}	
		    	            	}else if(pref.getBoolean("singlepaste", false)){
	            	            	try {
			    						mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
			    					} catch (Exception e1) {
			    						Toast.makeText(Ectx, "Removing listener went wrong", Toast.LENGTH_SHORT).show();
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
		menu2.add(android.view.Menu.NONE, id,android.view.Menu.NONE, "CB");
		menu2.findItem(id).setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	
	private void Open(Context mctx) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.dhm47.nativeclipboard","com.dhm47.nativeclipboard.ClipBoard"));
		int[] location = new int[2];
		Rect r = new Rect();
		Etextview.getLocationOnScreen(location);
		Etextview.getWindowVisibleDisplayFrame(r);
		double Precentage =((double)location[1])/(r.bottom-r.top);
		intent.putExtra("Keyheight",Precentage );
		mctx.startActivity(intent);
		}
	
	
	
		
		
	
}
