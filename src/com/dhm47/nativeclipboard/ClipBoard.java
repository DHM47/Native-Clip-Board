package com.dhm47.nativeclipboard;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhm47.nativeclipboard.comparators.PinnedFirst;
import com.dhm47.nativeclipboard.comparators.PinnedLast;


@SuppressLint({ "ClickableViewAccessibility", "InflateParams", "NewApi" })
public class ClipBoard extends Activity{
	
	private ClipboardManager mClipboardManager;
	//private LayoutInflater inflater;
	public static GridView gridView;
	private RelativeLayout mainLayout;
	private LinearLayout editLayout;
	private RelativeLayout bottomBar;
	private Context ctx;
	private RelativeLayout actionBar;
	private ImageView clear;
	private ImageView close;
	private ImageView add;
	private boolean adding;
	private ImageView overflow;
	private TextView textView;
	private TextView clipText;
	private EditText text;
	private EditText title;
	private TextView timeStamp;
	private static ClipAdapter clipAdapter;
	private SharedPreferences setting ;
	public static String backupS;
	public static int backupP;
	public static Clip backupClip;
	public static float backupX;
	public static float backupY;
	private int size;
	private int lPosition;
	static ClipData prevClip;
	private Snackbar Undo;
	
	private boolean clearall=false;
	private static List<String> mClipsOld = new ArrayList<String>();
	private static List<String> pinnedOld = new ArrayList<String>();
	
	int windowSize;
	int backgroundColor;
	int clipColor;
	int pinnedclipColor;
	int textColor;
	float textSize;
	
	boolean isUp;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	isUp=getIntent().getDoubleExtra("Keyheight", 0)>0.5;
    	overridePendingTransition(isUp? R.anim.open_slide_up : R.anim.open_slide_down,0); 
    	
		ctx=this;
		mClipboardManager =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		//inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setting = ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", Context.MODE_MULTI_PROCESS);
		clipAdapter = new ClipAdapter(ctx);
		
		windowSize=Util.px(setting.getInt("windowsize",280), ctx);
		backgroundColor=setting.getInt("bgcolor",0xFFFFFFFF);
		pinnedclipColor=setting.getInt("pincolor",0xFFCF5300);
		clipColor=setting.getInt("clpcolor",0xFFFFBB22);
		textColor=setting.getInt("txtcolor",0xFF664B0E);
		textSize=(float)(setting.getInt("txtsize",  20));
		
		try {
			FileInputStream fisc = ctx.openFileInput("Clips2.9");
			ObjectInputStream isc = new ObjectInputStream(fisc);
			ClipAdapter.mClips =  (List<Clip>) isc.readObject();
			size=ClipAdapter.mClips.size();
			isc.close();
		} catch (IOException e) {
			try {
				FileInputStream fisc = ctx.openFileInput("Clips");
				ObjectInputStream isc = new ObjectInputStream(fisc);
				mClipsOld =  (List<String>) isc.readObject();
				isc.close();
				FileInputStream fisp = ctx.openFileInput("Pinned");
				ObjectInputStream isp = new ObjectInputStream(fisp);
				pinnedOld =  (List<String>) isp.readObject();
				isp.close();
			} catch (IOException e1) {} catch (ClassNotFoundException e1) {}
			
			long x=System.currentTimeMillis();
			for (String text : mClipsOld) {
				ClipAdapter.mClips.add(new Clip(x--, text, "", false));
				}
			for (String text : pinnedOld) {
				ClipAdapter.mClips.add(new Clip(x--, text, "", true));
				}
			size=ClipAdapter.mClips.size();
			//setting.edit().putBoolean("first2.9", false).commit();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		/*if(setting.getBoolean("first2.9", true)){
		try {
			FileInputStream fisc = ctx.openFileInput("Clips");
			ObjectInputStream isc = new ObjectInputStream(fisc);
			mClipsOld =  (List<String>) isc.readObject();
			isc.close();
			FileInputStream fisp = ctx.openFileInput("Pinned");
			ObjectInputStream isp = new ObjectInputStream(fisp);
			pinnedOld =  (List<String>) isp.readObject();
			isp.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		long x=System.currentTimeMillis();
		for (String text : mClipsOld) {
			ClipAdapter.mClips.add(new Clip(x--, text, "", false));
			}
		for (String text : pinnedOld) {
			ClipAdapter.mClips.add(new Clip(x--, text, "", true));
			}
		size=ClipAdapter.mClips.size();
		setting.edit().putBoolean("first2.9", false).commit();
		}*/
		setContentView(R.layout.clip_board);
		prevClip=mClipboardManager.getPrimaryClip();
	}
		
	@SuppressLint("NewApi")
	@Override
    protected void onStart() {
        super.onStart();
        mainLayout=(RelativeLayout) findViewById(R.id.mainlayout);
		gridView =(GridView) mainLayout.findViewById(R.id.grid_view);
		
		actionBar=(RelativeLayout) findViewById(R.id.actionBar);
		clear= (ImageView) actionBar.findViewById(R.id.clear);
		close= (ImageView) actionBar.findViewById(R.id.close);
		add=(ImageView) actionBar.findViewById(R.id.add);
        
		textView =(TextView) mainLayout.findViewById(R.id.textViewB);
		clipText=(TextView) mainLayout.findViewById(R.id.clipText);
		
		editLayout=(LinearLayout) mainLayout.findViewById(R.id.EditView);
		text = (EditText) editLayout.findViewById(R.id.clipEdit);
        title =(EditText) editLayout.findViewById(R.id.clipTitleEdit);
		
        bottomBar=(RelativeLayout)mainLayout.findViewById(R.id.BottomBar);
        overflow =(ImageView) bottomBar.findViewById(R.id.overflow);
		timeStamp =(TextView) bottomBar.findViewById(R.id.timestamp);
    	
        
        mainLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Cancel();				
			}
		});
        
        actionBar.setBackgroundColor(clipColor);
        actionBar.setOnTouchListener(new OnTouchListener() {
			float y;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					y=event.getRawY();
					break;
				case MotionEvent.ACTION_UP:
					if(event.getRawY()-y>actionBar.getHeight()){
						Cancel();
						
					}
				}
				return true;
			}
		});
        
        bottomBar.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
        
        
        gridView.getLayoutParams().height=windowSize;
		gridView.setBackgroundColor(backgroundColor);
		gridView.setAdapter(clipAdapter);
		
		if(isUp){
	    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)gridView.getLayoutParams();
	    	params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	    	gridView.setLayoutParams(params);
	    	close.setRotation(180);
	    }else {
	    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)gridView.getLayoutParams();
	    	params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    	gridView.setLayoutParams(params);
		}

		if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			if(isUp){
		        getWindow().setStatusBarColor(darkenColor(clipColor, 0.8f));
		        getWindow().setNavigationBarColor(0x00ffffff);
			}else{
				getWindow().setStatusBarColor(0x00ffffff);
		        getWindow().setNavigationBarColor(darkenColor(clipColor, 0.8f));
			}
		EdgeEffect edgeEffectTop = new EdgeEffect(this);
		edgeEffectTop.setColor(clipColor);

		EdgeEffect edgeEffectBottom = new EdgeEffect(this);
		edgeEffectBottom.setColor(clipColor);

		try {
		    Field f1 = AbsListView.class.getDeclaredField("mEdgeGlowTop");
		    f1.setAccessible(true);
		    f1.set(gridView, edgeEffectTop);

		    Field f2 = AbsListView.class.getDeclaredField("mEdgeGlowBottom");
		    f2.setAccessible(true);
		    f2.set(gridView, edgeEffectBottom);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		}
		
		clear.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				builder.setMessage(R.string.clear_all_conf);
				builder.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearall=true;
						
						List<Clip> temp = new ArrayList<Clip>();
						for (Clip clip : ClipAdapter.mClips) {
					        if(clip.isPinned())temp.add(clip);
						}
						
						animateClearAll(temp);
					}
				});
				builder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				//builder.theme(isColorDark(backgroundColor)? Theme.DARK :Theme.LIGHT);
//				builder.callback(new MaterialDialog.ButtonCallback() {
//					@Override
//		            public void onPositive(MaterialDialog dialog) {}
//					
//					@Override
//			        public void onNegative(MaterialDialog dialog) {}
//				});
//				
				AppCompatDialog mDialog = builder.create();
				WindowManager.LayoutParams wlp = mDialog.getWindow().getAttributes();
				//wlp.gravity = (isUp?Gravity.TOP:Gravity.BOTTOM);
				wlp.dimAmount=0.7f;
				mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				mDialog.getWindow().setAttributes(wlp);
				mDialog.show();
							
			}
		});
		
		close.setOnTouchListener(new OnTouchListener() {
			float y;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					y=event.getRawY();
					break;
				case MotionEvent.ACTION_UP:
					if(event.getRawY()-y>actionBar.getHeight()){
						Cancel();
						return true;
					}
				}
				return false;
			}
		});
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Cancel();
				}
		});
		
		if(!isColorDark(clipColor)){
			close.setImageResource(R.drawable.ic_close_light);
			clear.setImageResource(R.drawable.ic_clear_all_light);
			add.setImageResource(R.drawable.ic_add_light);
		}
		add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Add();
				}
		});
		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				lPosition=position;
				if(ClipAdapter.mClips.get(position).getTitle().equals("")){
				textView.setText(ClipAdapter.mClips.get(position).getText());
                }else{
                textView.setText(ClipAdapter.mClips.get(position).getTitle());
                clipText.setText(ClipAdapter.mClips.get(position).getText());
                }
				textView.setBackgroundColor(ClipAdapter.mClips.get(position).isPinned() ? pinnedclipColor : clipColor);
				textView.setTextColor(textColor);
				textView.setTextSize(textSize);
				textView.setMovementMethod(new ScrollingMovementMethod());
				
				clipText.setTextColor(textColor);
				clipText.setTextSize(textSize);
				clipText.setMovementMethod(new ScrollingMovementMethod());

				bottomBar.setBackgroundColor(ClipAdapter.mClips.get(position).isPinned() ? pinnedclipColor : clipColor);
				editLayout.setBackgroundColor(ClipAdapter.mClips.get(position).isPinned() ? pinnedclipColor : clipColor);
                
		        text.setMovementMethod(new ScrollingMovementMethod());
		        text.setTextColor(textColor);
		        text.setTextSize(textSize);

		        title.setTextColor(textColor);
		        title.setTextSize(textSize);
		        title.setHintTextColor(textColor);
		           
				
				
		        final GestureDetector gDetector= new GestureDetector(ctx,new GestureDetector.OnGestureListener() {
					
									
					@Override
					public void onShowPress(MotionEvent e) {
						// TODO add lollipop like effect 
						
					}
					
					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
							float distanceY) {
						return false;
					}
					
					@Override
					public void onLongPress(MotionEvent e) {
						toGrid();
					}
					
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
							float velocityY) {
						return false;
					}
					
					@Override
					public boolean onDown(MotionEvent e) {
						return false;
					}

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						return false;
					}
				});
		        gDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
					
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						//Toast.makeText(ctx, "onSingleTapConfirmed", Toast.LENGTH_LONG).show();
						Select(position);
						return true;
					}
										
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						Edit();
						return true;
					}

					@Override
					public boolean onDoubleTapEvent(MotionEvent e) {
						return false;
					}
				});
		        gDetector.setIsLongpressEnabled(true);
		        
				textView.setOnTouchListener(new OnTouchListener() {
				    @Override
				    public boolean onTouch(View v, MotionEvent event) {
						return gDetector.onTouchEvent(event);
					}
				});
				clipText.setOnTouchListener(new OnTouchListener() {
				    @Override
				    public boolean onTouch(View v, MotionEvent event) {
						return gDetector.onTouchEvent(event);
					}
				});
				
				int color=ClipAdapter.mClips.get(position).isPinned() ? pinnedclipColor : clipColor;
				overflow.setImageResource(isColorDark(color) ? R.drawable.ic_action_overflow_dark : R.drawable.ic_action_overflow_light);
				overflow.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						OpenMenu();
					}
				});
				timeStamp.setTextColor(isColorDark(color)? 0xffffffff : 0x8c000000);
				DateFormat date = new SimpleDateFormat("dd/MM HH:mm",Locale.getDefault());
				timeStamp.setText(""+date.format(ClipAdapter.mClips.get(position).getTime()));
				toBig();
				return true;
			
				/*
				lPosition=position;
				textView =(TextView) mainLayout.findViewById(R.id.textViewB);
				textView.setText(ClipAdapter.mClips.get(position).getText());
				if(ClipAdapter.mClips.get(position).isPinned())textView.setBackgroundColor(setting.getInt("pincolor",0xFFCF5300));
				else textView.setBackgroundColor(setting.getInt("clpcolor",0xFFFFBB22));
				textView.setTextColor(setting.getInt("txtcolor",0xffffffff));
				textView.setTextSize((float)(setting.getInt("txtsize",  20)));
				textView.setMovementMethod(new ScrollingMovementMethod());
				textView.setOnTouchListener(new OnTouchListener() {
				    float startx;
				    float starty;
					@Override
				    public boolean onTouch(View v, MotionEvent event) {
				        if(event.getAction() == MotionEvent.ACTION_UP && NotFar(event)){
				        	Select(position);
				            return true;
				        }else if (event.getAction() == MotionEvent.ACTION_DOWN){
				        	startx=event.getRawX();
				        	starty=event.getRawY();}
				     
				        return false;
				    }
					private boolean NotFar(MotionEvent event){
						boolean returnVal = false;
						final int range = Util.px(15, ctx);
						if (Math.abs(startx - event.getRawX()) < range && Math.abs(starty - event.getRawY()) < range)
						returnVal = true;
						return returnVal;
						
					}
				});
								
				*/
				
			}
		});

		clipAdapter.registerDataSetObserver(new DataSetObserver() {
			public void onChanged() {
				//try {Undo.dismiss();} catch (Exception e) {}
				if(adding)return;
				if(ClipAdapter.mClips.size()<size && !clearall){//ClipAdapter.mClips.size()<size &&  && (ClipAdapter.mClips.size()!=0 || size==1)
				Undo=Snackbar.make(gridView, getResources().getString(R.string.deleted)+backupS , Snackbar.LENGTH_LONG);
				Undo.setAction(R.string.undo, new OnClickListener() {
					
					@Override
					public void onClick(View v) {
        				if(((gridView.getLastVisiblePosition())-gridView.getFirstVisiblePosition())>=(backupP-gridView.getFirstVisiblePosition())){//Not last item or before last
        					
	        			for(int x=(backupP-gridView.getFirstVisiblePosition());x<=gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition();x++){
	        					if(x<gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition()){
	        					gridView.getChildAt(x).animate()
	        					.x(gridView.getChildAt(x+1).getX())
	        					.y(gridView.getChildAt(x+1).getY())
	        					.setDuration(ctx.getResources().getInteger(
	        			                android.R.integer.config_mediumAnimTime))
	        					.start();
	        					}else{
	        						gridView.getChildAt(x).animate()
		        					.x(backupX)
		        					.y(backupY)
		        					.setDuration(ctx.getResources().getInteger(
		        			                android.R.integer.config_mediumAnimTime))
	        						.setListener(new AnimatorListenerAdapter() {
	                                    @Override
	                                    public void onAnimationEnd(Animator animation) {
	                                    	ClipAdapter.mClips.add(backupP, backupClip);
	                                    	clipAdapter.notifyDataSetChanged();
	                                    	size=ClipAdapter.mClips.size();
	                                    }
	                                }).start();
	        					}}
	        			
        				}else{
        					ClipAdapter.mClips.add(backupP, backupClip);
                        	clipAdapter.notifyDataSetChanged();
                        	size=ClipAdapter.mClips.size();
        				}
					}
				});
				Undo.show();
				clearall=false;
		      }
	
		    }
		});
		
	}
	@Override
	public void onBackPressed() {
		if(adding){
			if(title.getText().toString().equals("") && text.getText().toString().equals("")){
				actionBar.setVisibility(View.VISIBLE);
				bottomBar.setVisibility(View.INVISIBLE);
				ClipAdapter.mClips.remove(0);
				clipAdapter.notifyDataSetChanged();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
				adding=false;
				
				Animation anim = AnimationUtils.loadAnimation(this, R.anim.grow_in);
				anim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					@Override
					public void onAnimationRepeat(Animation animation) {}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						editLayout.setVisibility(View.INVISIBLE);
					}
				});
				editLayout.startAnimation(anim);
				
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				//Builder dialog=new MaterialDialog.Builder(ctx);
				String dialogtitle=getResources().getString(R.string.save);
				builder.setMessage(dialogtitle+" ?");
				builder.setPositiveButton(android.R.string.yes , new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Save();
					}
				});
				builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						editLayout.setVisibility(View.INVISIBLE);
						actionBar.setVisibility(View.VISIBLE);
						bottomBar.setVisibility(View.INVISIBLE);
						ClipAdapter.mClips.remove(0);
						clipAdapter.notifyDataSetChanged();
						getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
						adding=false;
					}
				});
				
//				dialog.theme(isColorDark(backgroundColor)? Theme.DARK :Theme.LIGHT);
//				dialog.callback(new MaterialDialog.ButtonCallback() {
//					@Override
//		            public void onPositive(MaterialDialog dialog) {
//						
//					}
//					
//					@Override
//			        public void onNegative(MaterialDialog dialog) {
//						editLayout.setVisibility(View.INVISIBLE);
//						actionBar.setVisibility(View.VISIBLE);
//						bottomBar.setVisibility(View.INVISIBLE);
//						ClipAdapter.mClips.remove(0);
//						clipAdapter.notifyDataSetChanged();
//						getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
//						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//						imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
//						adding=false;
//					}
//				});
				AppCompatDialog mDialog = builder.create();
				WindowManager.LayoutParams wlp = mDialog.getWindow().getAttributes();
				//wlp.gravity = (isUp?Gravity.TOP:Gravity.BOTTOM);
				wlp.dimAmount=0.7f;
				mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				mDialog.getWindow().setAttributes(wlp);
				mDialog.show();
				}
		}else if (actionBar.getVisibility()==View.INVISIBLE && textView.getVisibility()==View.VISIBLE){
			toGrid();
		}else if(actionBar.getVisibility()==View.INVISIBLE 
				&& (!text.getText().toString().equals(ClipAdapter.mClips.get(lPosition).getText()) || !title.getText().toString().equals(ClipAdapter.mClips.get(lPosition).getTitle()))){
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			String dialogtitle=getResources().getString(R.string.save);
			builder.setMessage(dialogtitle+" ?");
			builder.setPositiveButton(android.R.string.yes , new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Save();
				}
			});
			builder.setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					editLayout.setVisibility(View.INVISIBLE);
					textView.setVisibility(View.VISIBLE);
					clipText.setVisibility(View.VISIBLE);
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
				}
			});
//			dialog.theme(isColorDark(backgroundColor)? Theme.DARK :Theme.LIGHT);
//			dialog.callback(new MaterialDialog.ButtonCallback() {
//				@Override
//	            public void onPositive(MaterialDialog dialog) {
//					Save();
//				}
//				
//				@Override
//		        public void onNegative(MaterialDialog dialog) {
//					editLayout.setVisibility(View.INVISIBLE);
//					textView.setVisibility(View.VISIBLE);
//					clipText.setVisibility(View.VISIBLE);
//					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
//					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//					imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
//				}
//			});
			AppCompatDialog mDialog = builder.create();
			WindowManager.LayoutParams wlp = mDialog.getWindow().getAttributes();
			//wlp.gravity = (isUp?Gravity.TOP:Gravity.BOTTOM);
			wlp.dimAmount=0.7f;
			mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			mDialog.getWindow().setAttributes(wlp);
			mDialog.show();
			
						
		}else if(actionBar.getVisibility()==View.INVISIBLE){
			Save();
		}else {
		mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", "//NATIVECLIPBOARDCLOSE//"));	    
		super.onBackPressed();
		overridePendingTransition(0,isUp? R.anim.slide_up :R.anim.slide_down); 
		}
	}
	
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
	    if(keycode==KeyEvent.KEYCODE_MENU && close.getVisibility()==View.INVISIBLE){
	        	OpenMenu();
	            return true;
	    }
	    return super.onKeyDown(keycode, e);
	}
		
	@Override
	  public void onDestroy() {
		
		if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(this).toString().equals("//NATIVECLIPBOARDCLOSE//"))mClipboardManager.setPrimaryClip(prevClip);
		try {
			FileOutputStream fosc = ctx.openFileOutput("Clips2.9", Context.MODE_PRIVATE);
			ObjectOutputStream osc = new ObjectOutputStream(fosc);
			osc.writeObject(ClipAdapter.mClips);
			osc.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		super.onDestroy();	
	}

	public boolean isColorDark(int color){
	    double a = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
	    if(a<0.25){
	        return false; // It's a light color
	    }else{
	        return true; // It's a dark color
	    }
	}
	private void Cancel() {
		if(adding)if(ClipAdapter.mClips.get(0).getText().equals(""))ClipAdapter.mClips.remove(0);
		mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", "//NATIVECLIPBOARDCLOSE//"));
		ClipBoard.this.finish();
		overridePendingTransition(0,isUp? R.anim.slide_up :R.anim.slide_down);
	}
	
	public void Select(int position){
		mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", ClipAdapter.mClips.get(position).getText()));
		prevClip=ClipData.newPlainText("Text", ClipAdapter.mClips.get(position).getText());
		if(setting.getBoolean("singlepaste", false)){
			finish();
			overridePendingTransition(0,isUp? R.anim.slide_up :R.anim.slide_down);					
		}
	}
	
	public void toBig(){
		bottomBar.setVisibility(View.VISIBLE);
		actionBar.setVisibility(View.INVISIBLE);
		textView.setVisibility(View.VISIBLE);
		
		int mPosition=lPosition-gridView.getFirstVisiblePosition();
		
    	final int originalHeight = gridView.getChildAt(mPosition).getHeight();
    	final int originalWidth = gridView.getChildAt(mPosition).getWidth();
    	final float originalX=gridView.getChildAt(mPosition).getX();
    	final float originalY=gridView.getChildAt(mPosition).getY()+gridView.getY();
    	
    	final int finalHeight = gridView.getHeight()-gridView.getPaddingBottom()-gridView.getPaddingTop();
    	final int finalWidth = gridView.getWidth()-gridView.getPaddingRight()-gridView.getPaddingLeft();
    	final float finalX=gridView.getX()+gridView.getPaddingLeft();
    	final float finalY=gridView.getY()+gridView.getPaddingTop();
    	
		if (Build.VERSION.SDK_INT >= 21) {
    		final float originalZ=gridView.getElevation();
    		final float finalZ=actionBar.getElevation();
    		ValueAnimator animatorZ = ValueAnimator.ofFloat(originalZ,finalZ).setDuration(400);
    		animatorZ.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    			@Override
    			public void onAnimationUpdate(ValueAnimator valueAnimator) {
    				textView.setElevation((Float) valueAnimator.getAnimatedValue());
    			}
    		});	
    		animatorZ.start();
		}
	        ValueAnimator animatorH = ValueAnimator.ofInt(originalHeight,finalHeight).setDuration(400);
	        animatorH.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override	
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	                textView.setHeight((Integer) valueAnimator.getAnimatedValue());
	            }
	        });
	        ValueAnimator animatorW = ValueAnimator.ofInt(originalWidth,finalWidth).setDuration(400);
	        animatorW.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	                textView.setWidth((Integer) valueAnimator.getAnimatedValue());
	            }
	        });
	        ValueAnimator animatorX = ValueAnimator.ofFloat(originalX,finalX).setDuration(400);
	        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	            	textView.setX((Float) valueAnimator.getAnimatedValue());
	            }
	        });
	        ValueAnimator animatorY = ValueAnimator.ofFloat(originalY,finalY).setDuration(400);
	        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	            	textView.setY((Float) valueAnimator.getAnimatedValue());
	            }
	        });
	        animatorY.addListener(new AnimatorListenerAdapter() {
	            @Override
	            public void onAnimationEnd(Animator animation) {
	        		if(!ClipAdapter.mClips.get(lPosition).getTitle().equals("")){
	        		clipText.setX(textView.getX());
	        		clipText.setY(textView.getY()+textView.getLineBounds(textView.getLineCount()-1, null));
	        		clipText.setWidth(textView.getWidth());
	        		clipText.setHeight(textView.getHeight()-textView.getLineBounds(textView.getLineCount()-1, null));
	        		clipText.setVisibility(View.VISIBLE);}
	            }
	        });
	        animatorH.start();
	        animatorW.start();
	        animatorX.start();
	        animatorY.start();
	        
	}
	public void toGrid(){
		actionBar.setVisibility(View.VISIBLE);
		bottomBar.setVisibility(View.INVISIBLE);
		editLayout.setVisibility(View.INVISIBLE);
		clipText.setVisibility(View.INVISIBLE);
		
		final int mPosition=lPosition-gridView.getFirstVisiblePosition();
		final int originalHeight = gridView.getChildAt(mPosition).getHeight();
    	final int originalWidth = gridView.getChildAt(mPosition).getWidth();
    	final float originalX=gridView.getChildAt(mPosition).getX();
    	final float originalY=gridView.getChildAt(mPosition).getY()+gridView.getY();
    	
    	final int finalHeight = gridView.getHeight()-gridView.getPaddingBottom()-gridView.getPaddingTop();
    	final int finalWidth = gridView.getWidth()-gridView.getPaddingRight()-gridView.getPaddingLeft();
    	final float finalX=gridView.getX()+gridView.getPaddingLeft();
    	final float finalY=gridView.getY()+gridView.getPaddingTop();
    	
		if (Build.VERSION.SDK_INT >= 21) {
			final float originalZ=gridView.getElevation();
			final float finalZ=actionBar.getElevation();
			ValueAnimator animatorZ = ValueAnimator.ofFloat(finalZ,originalZ).setDuration(400);
	        animatorZ.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	            	textView.setElevation((Float) valueAnimator.getAnimatedValue());
	            }
	        });
	        animatorZ.start();
		}
    	
	        ValueAnimator animatorH = ValueAnimator.ofInt(finalHeight,originalHeight).setDuration(400);
	        animatorH.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	                textView.setHeight((Integer) valueAnimator.getAnimatedValue());
	            }
	        });
	        ValueAnimator animatorW = ValueAnimator.ofInt(finalWidth,originalWidth).setDuration(400);
	        animatorW.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	                textView.setWidth((Integer) valueAnimator.getAnimatedValue());
	            }
	        });
	        ValueAnimator animatorX = ValueAnimator.ofFloat(finalX,originalX).setDuration(400);
	        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	            	textView.setX((Float) valueAnimator.getAnimatedValue());
	            }
	        });
	        ValueAnimator animatorY = ValueAnimator.ofFloat(finalY,originalY).setDuration(400);
	        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator valueAnimator) {
	            	textView.setY((Float) valueAnimator.getAnimatedValue());
	            }
	        });
	        animatorY.addListener(new AnimatorListenerAdapter() {
	            @Override
	            public void onAnimationEnd(Animator animation) {
	            	textView.setVisibility(View.INVISIBLE);
	            	String sort=setting.getString("sort", "newfirst");
					if(sort.equals("pinnedfirst")){
						Collections.sort(ClipAdapter.mClips, new PinnedFirst());
						clipAdapter.notifyDataSetChanged();
					}else if(sort.equals("pinnedlast")){
						Collections.sort(ClipAdapter.mClips, new PinnedLast());
						clipAdapter.notifyDataSetChanged();
					}
	            }
	        });
	        animatorH.start();
	        animatorW.start();
	        animatorX.start();
	        animatorY.start();

	}
	public static void animRearrange(final int position,float xx, float yy, Context mContext){
		int x;
		if(gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition()!=(position-gridView.getFirstVisiblePosition())){
			for(x=gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition();x>(position-gridView.getFirstVisiblePosition());x--){
				if(x>(position-gridView.getFirstVisiblePosition()+1)){
				gridView.getChildAt(x).animate()
				.x(gridView.getChildAt(x-1).getX())
				.y(gridView.getChildAt(x-1).getY())
				.setDuration(mContext.getResources().getInteger(
		                android.R.integer.config_mediumAnimTime))
				.start();}
				else {
					gridView.getChildAt(x).animate()
					.x(xx)
					.y(yy)
					.setDuration(mContext.getResources().getInteger(
			                android.R.integer.config_mediumAnimTime))
					.setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                        	ClipAdapter.mClips.remove(position);
                        	clipAdapter.notifyDataSetChanged();
                        }
                    }).start();
				}}
			}else{
					ClipAdapter.mClips.remove(position);
					clipAdapter.notifyDataSetChanged();
				}
		
	}
	
	public void OpenMenu(){

		final PopupMenu popup = new PopupMenu(ctx, overflow);
           popup.getMenuInflater().inflate(R.menu.overflow, popup.getMenu());
           
           if(ClipAdapter.mClips.get(lPosition).isPinned()){popup.getMenu().findItem(R.id.pin).setVisible(false);popup.getMenu().findItem(R.id.del).setEnabled(false);}
           else popup.getMenu().findItem(R.id.unpin).setVisible(false);
        
           if (textView.getVisibility()==View.VISIBLE && !adding)popup.getMenu().findItem(R.id.save).setVisible(false);
           else popup.getMenu().findItem(R.id.edit).setVisible(false);
           
           if(adding)popup.getMenu().findItem(R.id.shrink).setVisible(false);
           
           popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case R.id.pin :
						ClipAdapter.mClips.get(lPosition).setPinned(true);
						textView.setBackgroundColor(pinnedclipColor);
						editLayout.setBackgroundColor(pinnedclipColor);
						bottomBar.setBackgroundColor(pinnedclipColor);
						gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition()).setBackgroundColor(pinnedclipColor);
						if(isColorDark(pinnedclipColor))overflow.setImageResource(R.drawable.ic_action_overflow_dark);
						else overflow.setImageResource(R.drawable.ic_action_overflow_light);
						break;
					case R.id.unpin:
						ClipAdapter.mClips.get(lPosition).setPinned(false);
						textView.setBackgroundColor(clipColor);
						editLayout.setBackgroundColor(clipColor);
						bottomBar.setBackgroundColor(clipColor);
						gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition()).setBackgroundColor(clipColor);
						if(isColorDark(clipColor))overflow.setImageResource(R.drawable.ic_action_overflow_dark);
						else overflow.setImageResource(R.drawable.ic_action_overflow_light);
						break;
					
					case R.id.del:
						backupClip=ClipAdapter.mClips.get(lPosition);
						backupP=lPosition;
						backupS=ClipAdapter.mClips.get(lPosition).getText();
						backupX=gridView.getChildAt(gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition()).getX();
						backupY=gridView.getChildAt(gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition()).getY();
						toGrid();
						final float xx=gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition()).getX();
						final float yy=gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition()).getY();
						
						gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition()).animate()
				         .translationX(gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition()).getWidth())
				         .alpha(0)
				         .setDuration(300)
				         .setStartDelay(405)
				         .setListener(new AnimatorListenerAdapter() {
				             @Override
				             public void onAnimationEnd(Animator animation) {
				                 animRearrange(lPosition, xx, yy, ctx);
				             }
				         });
						break;

					case R.id.edit :
						Edit();
						break;
					case R.id.save:
						Save();
						break;
					case R.id.shrink:
						toGrid();
						break;
					default:
						break;
					}
                	return clearall;}
            });

            popup.show();
	
	}
	public void Edit(){
		textView.setVisibility(View.INVISIBLE);
		RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) editLayout.getLayoutParams();
		params.height=textView.getHeight();
		params.width=textView.getWidth();
		editLayout.setLayoutParams(params);
		editLayout.setVisibility(View.VISIBLE);
		text.setText(ClipAdapter.mClips.get(lPosition).getText());
		title.setText(ClipAdapter.mClips.get(lPosition).getTitle());
		//text.setHeight(gridView.getHeight()-2*gridView.getPaddingBottom()-Util.px(42, ctx)-title.getHeight());
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	public void Save(){
		ClipAdapter.mClips.get(lPosition).setText(text.getText().toString());
		ClipAdapter.mClips.get(lPosition).setTitle(title.getText().toString());
		if(adding){
			textView.setHeight(editLayout.getHeight());
			textView.setWidth(editLayout.getWidth());
			textView.setX(editLayout.getX());
			textView.setY(editLayout.getY());
			if(title.getText().toString().equals("")){
				((TextView) gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition())).setText(text.getText());
				textView.setText(text.getText());
			}else{
				((TextView) gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition())).setText(title.getText());
				textView.setText(title.getText());
			}
			textView.setBackgroundColor(clipColor);
			textView.setTextColor(textColor);
			textView.setVisibility(View.VISIBLE);
			toGrid();
			
		}else if(!title.getText().toString().equals("")){
			((TextView) gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition())).setText(title.getText());
			textView.setText(title.getText());
			clipText.setText(text.getText());
			clipText.setX(textView.getX());
    		clipText.setY(textView.getY()+textView.getLineBounds(textView.getLineCount()-1, null));
    		clipText.setVisibility(View.VISIBLE);
    		textView.setVisibility(View.VISIBLE);
		}else {
			((TextView) gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition())).setText(text.getText());
			textView.setText(text.getText());
			clipText.setText("");
			textView.setVisibility(View.VISIBLE);
		}
		editLayout.setVisibility(View.INVISIBLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
		adding=false;
	}
	public void Add(){
		adding=true;
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		bottomBar.setBackgroundColor(clipColor);
		editLayout.setBackgroundColor(clipColor);
        
		text.setText("");
        text.setMovementMethod(new ScrollingMovementMethod());
        text.setTextColor(textColor);
        text.setTextSize(textSize);
        text.setHintTextColor(textColor);

        title.setText("");
        title.setTextColor(textColor);
        title.setTextSize(textSize);
        title.setHintTextColor(textColor);

		overflow.setImageResource(isColorDark(clipColor) ? R.drawable.ic_save_dark: R.drawable.ic_save_light);
		overflow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Save();
			}
		});
		
		timeStamp.setTextColor(isColorDark(clipColor)? 0xffffffff : 0x8c000000);
		DateFormat date = new SimpleDateFormat("dd/MM HH:mm",Locale.getDefault());
		timeStamp.setText(""+date.format(System.currentTimeMillis()));
		
		RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) editLayout.getLayoutParams();
		params.height=gridView.getHeight()-gridView.getPaddingBottom()-gridView.getPaddingTop();
		params.width=gridView.getWidth()-gridView.getPaddingRight()-gridView.getPaddingLeft();
		editLayout.setLayoutParams(params);
		editLayout.setVisibility(View.VISIBLE);
		bottomBar.setVisibility(View.VISIBLE);
		actionBar.setVisibility(View.INVISIBLE);

		Animation anim = AnimationUtils.loadAnimation(this, R.anim.grow_out);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				ClipAdapter.mClips.add(0, new Clip(System.currentTimeMillis(), "", "", false));
				lPosition=0;
				clipAdapter.notifyDataSetChanged();
			}
		});
		editLayout.startAnimation(anim);
		
		
	}
	
	public void animateClearAll(final List<Clip> temp){
		int lastDismissed = 0;
		for(int x=(gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition());x>=0;x--){

			if(!ClipAdapter.mClips.get(x).isPinned()){
				lastDismissed=(gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition()-x);
				gridView.getChildAt(x).animate()
	         .translationX(gridView.getChildAt(x).getWidth())
	         .alpha(0)
	         .setDuration(300).setStartDelay((gridView.getLastVisiblePosition()-gridView.getFirstVisiblePosition()-x)*100);
			}
			if(x==0){
				long dely =300+lastDismissed*100;
				CountDownTimer time=new CountDownTimer(dely, dely) {

					@Override
					public void onTick(long millisUntilFinished) {
					}
					
					@Override
					public void onFinish() {
						ClipAdapter.mClips=temp;
						clipAdapter.notifyDataSetChanged();
						
					}
				};
				time.start();
			}
			
		}
		
	}
	
	public static int darkenColor(int color, float factor) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= factor;
		return Color.HSVToColor(hsv);
	}
	
}
