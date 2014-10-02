package com.dhm47.nativeclipboard;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.dhm47.nativeclipboard.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.GridView;


@SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
public class ClipBoard extends Activity{
	private WindowManager windowManager;
	private ClipboardManager mClipboardManager;
	private LayoutInflater inflater;
	public static GridView gridView;
	private RelativeLayout mainLayout;
	private Context ctx;
	private Button clear;
	private ImageView close;
	private ImageView delete;
	private ImageView pin;
	private ImageView cancel;
	private TextView textView;
	private ClipAdapter clipAdapter;
	private SharedPreferences setting ;
	public static String backupS;
	public static int backupP;
	private int size;
	static ClipData prevClip;
	private View Undo;
	public static  List<String> pinned=new ArrayList<String>();
	private boolean clearall=false;
	
	


	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ctx=this;
		mClipboardManager =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		setting = ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4);
		clipAdapter = new ClipAdapter(ctx);
		try {
			FileInputStream fisc = ctx.openFileInput("Clips");
			ObjectInputStream isc = new ObjectInputStream(fisc);
			ClipAdapter.mClips =  (List<String>) isc.readObject();
			size=ClipAdapter.mClips.size();
			isc.close();
			FileInputStream fisp = ctx.openFileInput("Pinned");
			ObjectInputStream isp = new ObjectInputStream(fisp);
			pinned =  (List<String>) isp.readObject();
			isp.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		setContentView(R.layout.clip_board);
		prevClip=mClipboardManager.getPrimaryClip();
	}
		
	@Override
    protected void onStart() {
        super.onStart();
        mainLayout=(RelativeLayout) findViewById(R.id.mainlayout);
        mainLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Cancel();				
			}
		});
        
         
		gridView =(GridView) mainLayout.findViewById(R.id.grid_view);
		gridView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Util.px(setting.getInt("windowsize",280), ctx)));
        gridView.setBackgroundColor(setting.getInt("bgcolor",0x80E6E6E6));
		gridView.setAdapter(clipAdapter);
		
		if(getIntent().getDoubleExtra("Keyheight", 0)>0.5){
	    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)gridView.getLayoutParams();
	    	params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	    	gridView.setLayoutParams(params); 
	    }else {
	    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)gridView.getLayoutParams();
	    	params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    	gridView.setLayoutParams(params); 
		}
		
		clear= (Button) mainLayout.findViewById(R.id.clear);
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
						ClipAdapter.mClips.clear();
						ClipAdapter.mClips.addAll(pinned);
						clipAdapter.notifyDataSetChanged();
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
		
		close= (ImageView) mainLayout.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Cancel();
				}
		});
		
		if(isColorDark(setting.getInt("bgcolor",0x80E6E6E6))){
			close.setImageResource(R.drawable.ic_close_dark);
			clear.setTextColor(0xFFCCCCCC);
		}
				
		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				
				textView =(TextView) mainLayout.findViewById(R.id.textViewB);
				textView.setText(ClipAdapter.mClips.get(position));
				if(pinned.contains(ClipAdapter.mClips.get(position)))textView.setBackgroundColor(setting.getInt("pincolor",0xFFCF5300));
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
								
				delete =(ImageView) mainLayout.findViewById(R.id.imageViewBD);
				delete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ClipAdapter.mClips.remove(position);
						clipAdapter.notifyDataSetChanged();
						toGrid();
					}
				});
				
				cancel =(ImageView) mainLayout.findViewById(R.id.imageViewBC);
				cancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						toGrid();
					}
				});
				pin=(ImageView) mainLayout.findViewById(R.id.imageViewBS);
				pin.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(delete.getVisibility()==0){
							pinned.add(ClipAdapter.mClips.get(position));
							delete.setVisibility(View.INVISIBLE);
							textView.setBackgroundColor(setting.getInt("pincolor",0xFFCF5300));
							gridView.getChildAt(position-gridView.getFirstVisiblePosition()).setBackgroundColor(setting.getInt("pincolor",0xFFFF3300));
						}else{
							pinned.remove(ClipAdapter.mClips.get(position));
							delete.setVisibility(View.VISIBLE);
							textView.setBackgroundColor(setting.getInt("clpcolor",0xFFFFBB22));
							gridView.getChildAt(position-gridView.getFirstVisiblePosition()).setBackgroundColor(setting.getInt("clpcolor",0xFFFFBB22));
						}
					}
				});
				toBig();
				if(pinned.contains(ClipAdapter.mClips.get(position)))delete.setVisibility(View.INVISIBLE);
				return true;
			}
		});

		clipAdapter.registerDataSetObserver(new DataSetObserver() {
			public void onChanged() {
				try {windowManager.removeView(Undo);} catch (Exception e) {}
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
						// TODO add addition animation for last item
						timeout.cancel();
	        			windowManager.removeView(Undo);
	        			
        				if(((gridView.getLastVisiblePosition())-gridView.getFirstVisiblePosition())>(backupP-gridView.getFirstVisiblePosition())){//Not last item or before last
        					
	        			for(int x=(backupP-ClipBoard.gridView.getFirstVisiblePosition());x<ClipBoard.gridView.getLastVisiblePosition()-ClipBoard.gridView.getFirstVisiblePosition();x++){
	        					if(x<ClipBoard.gridView.getLastVisiblePosition()-ClipBoard.gridView.getFirstVisiblePosition()-1){
	        					ClipBoard.gridView.getChildAt(x).animate()
	        					.x(ClipBoard.gridView.getChildAt(x+1).getX())
	        					.y(ClipBoard.gridView.getChildAt(x+1).getY())
	        					.setDuration(ctx.getResources().getInteger(
	        			                android.R.integer.config_mediumAnimTime))
	        					.start();
	        					}else{
	        						ClipBoard.gridView.getChildAt(x).animate()
		        					.x(ClipBoard.gridView.getChildAt(x+1).getX())
		        					.y(ClipBoard.gridView.getChildAt(x+1).getY())
		        					.setDuration(ctx.getResources().getInteger(
		        			                android.R.integer.config_mediumAnimTime))
	        						.setListener(new AnimatorListenerAdapter() {
	                                    @Override
	                                    public void onAnimationEnd(Animator animation) {
	                                    	ClipAdapter.mClips.add(backupP, backupS);
	                                    	clipAdapter.notifyDataSetChanged();
	                                    	size=ClipAdapter.mClips.size();
	                                    }
	                                }).start();
	        					}}
	        			
        				}else{
        					ClipAdapter.mClips.add(backupP, backupS);
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
		if (clear.getVisibility()==View.INVISIBLE){
			toGrid();
		}else {
		mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", "//NATIVECLIPBOARDCLOSE//"));	    
		super.onBackPressed();
		if(getIntent().getDoubleExtra("Keyheight", 0)>0.5){
			overridePendingTransition(0, R.anim.slide_up); 
	    }else {
	    	overridePendingTransition(0, R.anim.slide_down); 
		}
		
		}
	}
		
	@Override
	  public void onDestroy() {
		
		if(mClipboardManager.getPrimaryClip().getItemAt(0).coerceToText(this).toString().equals("//NATIVECLIPBOARDCLOSE//"))mClipboardManager.setPrimaryClip(prevClip);
		try {windowManager.removeView(Undo);} catch (Exception e) {}
		try {
			FileOutputStream fosc = ctx.openFileOutput("Clips", Context.MODE_PRIVATE);
			ObjectOutputStream osc = new ObjectOutputStream(fosc);
			osc.writeObject(ClipAdapter.mClips);
			osc.close();
			FileOutputStream fosp = ctx.openFileOutput("Pinned", Context.MODE_PRIVATE);
			ObjectOutputStream osp = new ObjectOutputStream(fosp);
			osp.writeObject(pinned);
			osp.close();
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
		mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", "//NATIVECLIPBOARDCLOSE//"));
		try {windowManager.removeView(Undo);} catch (Exception e) {}
		ClipBoard.this.finish();
		if(getIntent().getDoubleExtra("Keyheight", 0)>0.5){
			overridePendingTransition(0, R.anim.slide_up); 
	    }else {
	    	overridePendingTransition(0, R.anim.slide_down); 
		}
	}
	
	public void Select(int position){
		mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", ClipAdapter.mClips.get(position)));
		prevClip=ClipData.newPlainText("Text", ClipAdapter.mClips.get(position));
		
	}
	
	public void toBig(){
		close.setVisibility(View.INVISIBLE);
		clear.setVisibility(View.INVISIBLE);
		delete.setVisibility(View.VISIBLE);
		pin.setVisibility(View.VISIBLE);
		cancel.setVisibility(View.VISIBLE);
		textView.setVisibility(View.VISIBLE);
				
	}
	public void toGrid(){
		close.setVisibility(View.VISIBLE);
		clear.setVisibility(View.VISIBLE);
		delete.setVisibility(View.INVISIBLE);
		pin.setVisibility(View.INVISIBLE);
		cancel.setVisibility(View.INVISIBLE);
		textView.setVisibility(View.INVISIBLE);
		
	}
}
