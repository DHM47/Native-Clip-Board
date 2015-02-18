package com.dhm47.nativeclipboard;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhm47.nativeclipboard.comparators.PinnedFirst;
import com.dhm47.nativeclipboard.comparators.PinnedLast;


@SuppressLint({ "ClickableViewAccessibility", "InflateParams" })
public class ClipBoard extends Activity{
	
	private WindowManager windowManager;
	private ClipboardManager mClipboardManager;
	private LayoutInflater inflater;
	public static GridView gridView;
	private RelativeLayout mainLayout;
	private LinearLayout editLayout;
	private RelativeLayout bottomBar;
	private Context ctx;
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
	private View Undo;
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
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		setting = ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", Context.MODE_MULTI_PROCESS);
		clipAdapter = new ClipAdapter(ctx);
		
		windowSize=Util.px(setting.getInt("windowsize",280), ctx);
		backgroundColor=setting.getInt("bgcolor",0x80E6E6E6);
		pinnedclipColor=setting.getInt("pincolor",0xFFCF5300);
		clipColor=setting.getInt("clpcolor",0xFFFFBB22);
		textColor=setting.getInt("txtcolor",0xffffffff);
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
		
	@Override
    protected void onStart() {
        super.onStart();
        mainLayout=(RelativeLayout) findViewById(R.id.mainlayout);
		gridView =(GridView) mainLayout.findViewById(R.id.grid_view);
		clear= (ImageView) mainLayout.findViewById(R.id.clear);
		close= (ImageView) mainLayout.findViewById(R.id.close);
		add=(ImageView) mainLayout.findViewById(R.id.add);
        textView =(TextView) mainLayout.findViewById(R.id.textViewB);
		clipText=(TextView) mainLayout.findViewById(R.id.clipText);
		bottomBar=(RelativeLayout)mainLayout.findViewById(R.id.BottomBar);
		editLayout=(LinearLayout) mainLayout.findViewById(R.id.EditView);
		text = (EditText) editLayout.findViewById(R.id.clipEdit);
        title =(EditText) editLayout.findViewById(R.id.clipTitleEdit);
		overflow =(ImageView) bottomBar.findViewById(R.id.overflow);
		timeStamp =(TextView) bottomBar.findViewById(R.id.timestamp);

        
        mainLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Cancel();				
			}
		});
        
         
		gridView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, windowSize));
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
		
		clear.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				AlertDialog dialog ;
				AlertDialog.Builder confirm =new AlertDialog.Builder(ctx);
				confirm.setTitle(R.string.clear_all_conf);
				confirm.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					
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
				confirm.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				dialog = confirm.create();
				dialog.show();
							
			}
		});
		
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Cancel();
				}
		});
		
		if(isColorDark(backgroundColor)){
			close.setImageResource(R.drawable.ic_close_dark);
			clear.setImageResource(R.drawable.ic_clear_all_dark);
			add.setImageResource(R.drawable.ic_add_dark);
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
				timeStamp.setTextColor(textColor);
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
				try {windowManager.removeView(Undo);} catch (Exception e) {}
				if(adding)return;
				if(ClipAdapter.mClips.size()<size && !clearall){//ClipAdapter.mClips.size()<size &&  && (ClipAdapter.mClips.size()!=0 || size==1)
					Undo = inflater.inflate(R.layout.undo,null);
					final CountDownTimer timeout=new CountDownTimer(3000, 3000) {
			        	public void onTick(long millisUntilFinished) {}
			            public void onFinish() {
			            	try {windowManager.removeView(Undo);} catch (Exception e) {}
			            	size=ClipAdapter.mClips.size();
			            }
			        };
			         
				WindowManager.LayoutParams undoparams = new WindowManager.LayoutParams(
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_PHONE,
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
						PixelFormat.TRANSLUCENT);
						undoparams.gravity = Gravity.BOTTOM | Gravity.CENTER;
						undoparams.y=Util.px(60, ctx);
						undoparams.windowAnimations=android.R.style.Animation_Toast;
				
				TextView button =(TextView) Undo.findViewById(R.id.undobutton);
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO add addition animation for last item DONE
						timeout.cancel();
	        			windowManager.removeView(Undo);
	        			
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
				TextView text =(TextView) Undo.findViewById(R.id.undotxt);
				text.setText(getResources().getString(R.string.deleted)+backupS+" ");
				windowManager.addView(Undo, undoparams);
				timeout.start();
				clearall=false;
		      }
	
		    }
		});
		
	}
	@Override
	public void onBackPressed() {
		if(adding){
			if(title.getText().toString().equals("") && text.getText().toString().equals("")){
				editLayout.setVisibility(View.INVISIBLE);
				bottomBar.setVisibility(View.INVISIBLE);
				clear.setVisibility(View.VISIBLE);
				ClipAdapter.mClips.remove(0);
				clipAdapter.notifyDataSetChanged();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
				adding=false;
			}else{
			AlertDialog dialog ;
			AlertDialog.Builder confirm =new AlertDialog.Builder(ctx);
			String dialogtitle=getResources().getString(R.string.save);
			confirm.setTitle(dialogtitle+" ?");
			confirm.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Save();
					}
			});
			confirm.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					editLayout.setVisibility(View.INVISIBLE);
					bottomBar.setVisibility(View.INVISIBLE);
					clear.setVisibility(View.VISIBLE);
					ClipAdapter.mClips.remove(0);
					clipAdapter.notifyDataSetChanged();
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
					adding=false;
				}
			});
			dialog = confirm.create();
			dialog.show();}
		}else if (clear.getVisibility()==View.INVISIBLE && textView.getVisibility()==View.VISIBLE){
			toGrid();
		}else if(clear.getVisibility()==View.INVISIBLE 
				&& (!text.getText().toString().equals(ClipAdapter.mClips.get(lPosition).getText()) || !title.getText().toString().equals(ClipAdapter.mClips.get(lPosition).getTitle()))){
			AlertDialog dialog ;
			AlertDialog.Builder confirm =new AlertDialog.Builder(ctx);
			String dialogtitle=getResources().getString(R.string.save);
			confirm.setTitle(dialogtitle+" ?");
			confirm.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Save();
					}
			});
			confirm.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				
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
			dialog = confirm.create();
			dialog.show();
						
		}else if(clear.getVisibility()==View.INVISIBLE){
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
		try {windowManager.removeView(Undo);} catch (Exception e) {}
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
	    if(a<0.5){
	        return false; // It's a light color
	    }else{
	        return true; // It's a dark color
	    }
	}
	private void Cancel() {
		if(ClipAdapter.mClips.get(0).getText().equals(""))ClipAdapter.mClips.remove(0);
		mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", "//NATIVECLIPBOARDCLOSE//"));
		try {windowManager.removeView(Undo);} catch (Exception e) {}
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
		close.setVisibility(View.INVISIBLE);
		clear.setVisibility(View.INVISIBLE);
		textView.setVisibility(View.VISIBLE);
		
		int mPosition=lPosition-gridView.getFirstVisiblePosition();
		
    	final int originalHeight = gridView.getChildAt(mPosition).getHeight();
    	final int originalWidth = gridView.getChildAt(mPosition).getWidth();
    	final float originalX=gridView.getChildAt(mPosition).getX();
    	final float originalY=gridView.getChildAt(mPosition).getY()+gridView.getY();
    	
    	final int finalHeight = gridView.getHeight()-2*gridView.getPaddingBottom()-Util.px(35, ctx);
    	final int finalWidth = gridView.getWidth()-gridView.getPaddingRight()-gridView.getPaddingLeft();
    	final float finalX=gridView.getX()+gridView.getPaddingLeft();
    	final float finalY=gridView.getY()+gridView.getPaddingBottom()	;
    	
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
	        		bottomBar.setVisibility(View.VISIBLE);
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
		close.setVisibility(View.VISIBLE);
		clear.setVisibility(View.VISIBLE);
		bottomBar.setVisibility(View.INVISIBLE);
		editLayout.setVisibility(View.INVISIBLE);
		clipText.setVisibility(View.INVISIBLE);
		
		final int mPosition=lPosition-gridView.getFirstVisiblePosition();
		final int originalHeight = gridView.getChildAt(mPosition).getHeight();
    	final int originalWidth = gridView.getChildAt(mPosition).getWidth();
    	final float originalX=gridView.getChildAt(mPosition).getX();
    	final float originalY=gridView.getChildAt(mPosition).getY()+gridView.getY();
    	
    	final int finalHeight = gridView.getHeight()-2*gridView.getPaddingBottom();
    	final int finalWidth = gridView.getWidth()-gridView.getPaddingRight()-gridView.getPaddingLeft();
    	final float finalX=gridView.getX()+gridView.getPaddingLeft();
    	final float finalY=gridView.getY()+gridView.getPaddingBottom()	;
    	
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
			bottomBar.setVisibility(View.INVISIBLE);
			clear.setVisibility(View.VISIBLE);
			if(title.getText().toString().equals(""))((TextView) gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition())).setText(text.getText());
			else((TextView) gridView.getChildAt(lPosition-gridView.getFirstVisiblePosition())).setText(title.getText());
			
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
		clear.setVisibility(View.INVISIBLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		bottomBar.setBackgroundColor(clipColor);
		editLayout.setBackgroundColor(clipColor);
        
		text.setText("");
        text.setMovementMethod(new ScrollingMovementMethod());
        text.setTextColor(textColor);
        text.setTextSize(textSize);
        text.setHint("Type Clip text");
        text.setHintTextColor(textColor);

        title.setText("");
        title.setTextColor(textColor);
        title.setTextSize(textSize);
        title.setHintTextColor(textColor);

		overflow.setImageResource(isColorDark(clipColor) ? R.drawable.ic_action_overflow_dark : R.drawable.ic_action_overflow_light);
		overflow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OpenMenu();
			}
		});
		
		timeStamp.setTextColor(textColor);
		DateFormat date = new SimpleDateFormat("dd/MM HH:mm",Locale.getDefault());
		timeStamp.setText(""+date.format(System.currentTimeMillis()));
		
		RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) editLayout.getLayoutParams();
		params.height=gridView.getHeight()-2*gridView.getPaddingBottom()-Util.px(35, ctx);
		params.width=gridView.getWidth()-gridView.getPaddingRight()-gridView.getPaddingLeft();
		editLayout.setLayoutParams(params);
		editLayout.setVisibility(View.VISIBLE);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.grow);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				bottomBar.setVisibility(View.VISIBLE);
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
	
}
