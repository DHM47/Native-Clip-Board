package com.dhm47.nativeclipboard;






import android.annotation.SuppressLint;
import android.app.ActivityManager.TaskDescription;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;




public class Setting extends ActionBarActivity implements SettingsListFragment.Callbacks {
    static Context ctx;
    Toolbar mToolbar;
    //RelativeLayout toolbarContainer;
    SharedPreferences pref;
    boolean isCatagory;
	boolean mTwoPane;
	boolean isBlacklist;
	FloatingActionButton fab;
	
	public static Dialog dialog = null;
    private static final int MENU_ADD = 0;
    private static final int MENU_HELP = 1;
    
    private Callbacks mCallbacks = sDummyCallbacks;

	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onAddSelected();
	}
    private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onAddSelected() {
		}
	};
	
	@SuppressLint({ "InlinedApi", "NewApi" })
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx=this;
		setContentView(R.layout.preference_activity);
		if (findViewById(R.id.prefrence_catagory_container) != null) {
			mTwoPane = true;
			onCatagorySelected("theme");
		}else{
			getFragmentManager().beginTransaction().replace(R.id.container, new SettingsListFragment()).commit();
			
		}
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
	    mToolbar.setTitle(getTitle());
	    
		isCatagory=false;
		
		setSupportActionBar(mToolbar);
		
		fab= (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, ClipBoard.class);
				ctx.startActivity(intent);
			}
		});
        
		
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //getWindow().setStatusBarColor(getResources().getColor(R.color.dark_deep_purple));
            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
    		TaskDescription taskDescription = new TaskDescription(getResources().getString(R.string.app_name), icon, getResources().getColor(R.color.deep_purple));
    		setTaskDescription(taskDescription);
        }

/*		if(!getTitle().equals(getResources().getString(R.string.app_name))){
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
		}

		pref=getSharedPreferences("com.dhm47.nativeclipboard_preferences", Context.MODE_MULTI_PROCESS);
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		 View content = root.getChildAt(0);
		 toolbarContainer = (RelativeLayout) View.inflate(this, R.layout.preference_activity, null);

		 root.removeAllViews();
		 
	    	
		 toolbarContainer.addView(content);
		 RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)content.getLayoutParams();
		 params.addRule(RelativeLayout.BELOW,R.id.toolbar);
		 content.setLayoutParams(params);
		 root.addView(toolbarContainer);

        mToolbar = (Toolbar) toolbarContainer.findViewById(R.id.toolbar);
        mToolbar.setTitle(getTitle());
        //mToolbar.setNavigationIcon(new ColorDrawable(ctx.getResources().getColor(android.R.color.transparent)));

		RelativeLayout.LayoutParams paramsbar = (RelativeLayout.LayoutParams)mToolbar.getLayoutParams();

		
		ImageView support= (ImageView) toolbarContainer.findViewById(R.id.support);
		RelativeLayout.LayoutParams paramssupport = (RelativeLayout.LayoutParams)support.getLayoutParams();
		paramssupport.setMargins(paramssupport.leftMargin, paramsbar.height/4, paramssupport.rightMargin, paramssupport.bottomMargin);
		paramssupport.height=paramsbar.height/2;
        support.setLayoutParams(paramssupport);
		support.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/native-clip-board-beta-t2784682"));
				ctx.startActivity(intent1);
	            
			}
		});
		ImageView test= (ImageView) toolbarContainer.findViewById(R.id.test);
        RelativeLayout.LayoutParams paramstest = (RelativeLayout.LayoutParams)test.getLayoutParams();
        paramstest.setMargins(paramstest.leftMargin, paramsbar.height/4, paramstest.rightMargin, paramstest.bottomMargin);
		paramstest.height=paramsbar.height/2;
        test.setLayoutParams(paramstest);
        test.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, ClipBoard.class);
				ctx.startActivity(intent);
			}
		});
        

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.dark_deep_purple));
            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
    		TaskDescription taskDescription = new TaskDescription(getResources().getString(R.string.app_name), icon, getResources().getColor(R.color.deep_purple));
    		setTaskDescription(taskDescription);
        }
        /*setContentView(R.layout.preference_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingFragment()).commit();*/
		
	}
	

	/*@Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.layout.preference_headers, target);
        
        //if(getSharedPreferences("com.dhm47.nativeclipboard_preferences", Context.MODE_MULTI_PROCESS).getBoolean("monitorservice", false))
        //target.get(2).summary="Black list doesn't work with compatablilty mode";
    }
	
	@Override
	public void onHeaderClick(Header header, int position) {
		switch (position) {
		case 2:
			if(pref.getBoolean("monitorservice", false)){
				//header.summary="Blacklist doesn't work with compatablilty mode";
			}else{
			Intent intent = new Intent(ctx, Blacklist.class);
			ctx.startActivity(intent);}
			break;
		case 3:
			final MaterialDialog.Builder dialogSort = new MaterialDialog.Builder(Setting.this);
	        dialogSort.title(R.string.sort);
	        dialogSort.positiveText(null);
	        dialogSort.negativeText("Cancel");
	        dialogSort.items(getResources().getStringArray(R.array.sortby));
	        List<String> sortval = Arrays.asList(getResources().getStringArray(R.array.sortvalues));
	        dialogSort.itemsCallbackSingleChoice(sortval.indexOf(pref.getString("sort", "newfirst")), new MaterialDialog.ListCallback() {
	          @SuppressWarnings("unchecked")
			@Override
	          public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
	            //onClick(null, DialogInterface.BUTTON_POSITIVE);
	            dialog.dismiss();

	            if (which >= 0 && getResources().getStringArray(R.array.sortvalues) != null) {
	            	String sort = getResources().getStringArray(R.array.sortvalues)[which].toString();
	              pref.edit().putString("sort", sort).commit();
	              try {//Read Clips
						FileInputStream fis = ctx.openFileInput("Clips2.9");
						ObjectInputStream is = new ObjectInputStream(fis);
						mClip =  (List<Clip>) is.readObject();
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					if(sort.equals("newfirst")){
						Collections.sort(mClip, new NewFirst());
					}else if(sort.equals("pinnedfirst")){
						Collections.sort(mClip, new PinnedFirst());
					}else if(sort.equals("pinnedlast")){
						Collections.sort(mClip, new PinnedLast());
					}
					
			    	try {//Write
			              FileOutputStream fos = ctx.openFileOutput("Clips2.9", Context.MODE_PRIVATE);
			              ObjectOutputStream os = new ObjectOutputStream(fos);
			              os.writeObject(mClip);
			              os.close();
					} catch (Exception e) {}
		
	            }
	          }
	        });
	        dialogSort.show();
			break;
		
		}
		super.onHeaderClick(header, position);
	}
	@TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragmentName.equals(SettingFragment.class.getName());
    }*/
	@Override
	public void onBackPressed() {
		if(isCatagory){
			Back();
		}else{
		super.onBackPressed();
		}
	}
	@Override
	public void onCatagorySelected(String key) {
		//Toast.makeText(ctx, "Got Click", Toast.LENGTH_SHORT).show();
		Bundle arguments = new Bundle();
		arguments.putString("category", key);
		SettingFragment settings= new SettingFragment();
		settings.setArguments(arguments);
		Blacklist blacklist= new Blacklist();
		mCallbacks = (Callbacks) blacklist;
		
		if (mTwoPane) {
			if(key.equals("blacklist")){		
				getFragmentManager().beginTransaction().replace(R.id.prefrence_catagory_container,blacklist).commit();
				isBlacklist=true;
				invalidateOptionsMenu();
			}else{
				getFragmentManager().beginTransaction().replace(R.id.prefrence_catagory_container,settings).commit();
				isBlacklist=false;
				invalidateOptionsMenu();
			}
			TextView title=(TextView) findViewById(R.id.prefrence_catagory).findViewById(R.id.prefrence_catagory_title);
			if (key.equals("theme")) {
                title.setText(R.string.category_theme);
            } else if (key.equals("size")) {
            	title.setText(R.string.category_sizes);
            }else if (key.equals("advanced")){
            	title.setText(R.string.category_advanced);
            }else if(key.equals("blacklist")){
            	title.setText(R.string.blacklist);
            }
		}else{
			if(key.equals("blacklist")){		
				getFragmentManager().beginTransaction().setCustomAnimations(R.animator.slide_left_in, R.animator.slide_left_out).replace(R.id.container,blacklist).commit();
            	isBlacklist=true;
            	invalidateOptionsMenu();
			}else{
				getFragmentManager().beginTransaction().setCustomAnimations(R.animator.slide_left_in, R.animator.slide_left_out).replace(R.id.container,settings).commit();
				isBlacklist=false;
				invalidateOptionsMenu();
			}
			isCatagory=true	;
			//MenuItem item = menu.findItem(R.id.addAction);
			if (key.equals("theme")) {
                mToolbar.setTitle(R.string.category_theme);
            } else if (key.equals("size")) {
                mToolbar.setTitle(R.string.category_sizes);
            }else if (key.equals("advanced")){
                mToolbar.setTitle(R.string.category_advanced);
                fab.hide();
            }else if(key.equals("blacklist")){
            	mToolbar.setTitle(R.string.blacklist);
                fab.hide();
            }
			mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
			mToolbar.setNavigationOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
					Back();
			}
		});
		}
	}
	public void Back(){
		getFragmentManager().beginTransaction().setCustomAnimations(R.animator.slide_right_in, R.animator.slide_right_out).replace(R.id.container, new SettingsListFragment()).commit();
		isCatagory=false;
		isBlacklist=false;
		mCallbacks=sDummyCallbacks;
		mToolbar.setNavigationIcon(null);
		mToolbar.setTitle(getTitle());
		invalidateOptionsMenu();
		fab.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		if(isBlacklist){
			menu.add(Menu.NONE, 0, 0, R.string.add)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, 1, 1, R.string.help)
        	.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}else if(mToolbar.getTitle().equals(getTitle())){
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.setting_actions, menu);
		}
	    return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	       case R.id.action_support:
	        	Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/native-clip-board-beta-t2784682"));
				ctx.startActivity(intent1);
	            return true;
	       case MENU_HELP:
	        	AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
	        	alertDialog.setTitle(R.string.help);
	        	alertDialog.setMessage(getString(R.string.blacklist_help));
	        	alertDialog.show();
	        	return true;
	        case MENU_ADD:
	        	mCallbacks.onAddSelected();
	        	return true;
	    
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}


	
}