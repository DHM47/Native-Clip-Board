/*
 * Copyright (C) 2012 The CyanogenMod Project
 * This code has been modified. Portions copyright (C) 2012, ParanoidAndroid Project.
 * This code has been modified further. Portions copyright (C) 2013, XuiMod.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2013 XuiMod
 * Based on source obtained from the ParanoidAndroid Project, Copyright (C) 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dhm47.nativeclipboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class ApplicationsDialog {

    public PackageManager mPackageManager;
    public List<ResolveInfo> mInstalledApps;
    
    public AppAdapter createAppAdapter(Context context, List<ResolveInfo> installedAppsInfo){
        mPackageManager = context.getPackageManager();
        return new AppAdapter(context, installedAppsInfo);
    }

    public class AppItem implements Comparable<AppItem> {
        public CharSequence title;
        public String packageName;
        public Drawable icon;

        @Override
        public int compareTo(AppItem another) {
            return this.title.toString().compareTo(another.title.toString());
        }
    }

    public class AppAdapter extends BaseAdapter implements Filterable{
        protected List<ResolveInfo> mInstalledAppInfo;
        protected List<AppItem> mInstalledApps = new LinkedList<AppItem>();
        protected List<ResolveInfo> temporarylist; 
        private Context mContext;
        
        public AppAdapter(Context context, List<ResolveInfo> installedAppsInfo) {
            mInstalledAppInfo = installedAppsInfo;
            mContext = context;
            temporarylist = mInstalledAppInfo;
        }

        private void reloadList() {
            final Handler handler = new Handler();
            new Thread(new Runnable() {
            	@Override
            	public void run() {
            		synchronized (mInstalledApps) {
            			mInstalledApps.clear();
            			for (ResolveInfo info : temporarylist) {
            				final AppItem item = new AppItem();
            				item.title = info.loadLabel(mPackageManager);
                            item.icon = info.loadIcon(mPackageManager);
                            item.packageName = info.activityInfo.packageName;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    int index = Collections.binarySearch(mInstalledApps, item);
                                    if (index < 0) {
                                        index = -index - 1;
                                        mInstalledApps.add(index, item);
                                    }
                                    notifyDataSetChanged();
                                    
                                }
                            });
                        }
            			updateProgressBar();
                    }
                }
            }).start();
        }

        private void updateProgressBar(){
        	try{
            	View progressBar = Blacklist.dialog.findViewById(R.id.progress_bar);
            	progressBar.setVisibility(View.GONE);
            }catch(Exception e){
            	
            }
        }
        public void update() {
            reloadList();
        }

        @Override
        public int getCount() {
            return mInstalledApps.size();
        }

        @Override
        public AppItem getItem(int position) {
            return mInstalledApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mInstalledApps.get(position).packageName.hashCode();
        }

        @SuppressLint("InflateParams")
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.view_app_dialog, null, false);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.title = (TextView) convertView.findViewById(android.R.id.title);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
            }
            AppItem applicationInfo = getItem(position);

            if (holder.title != null) {
                holder.title.setText(applicationInfo.title);
            }
            if (holder.subtitle != null) {
                holder.subtitle.setText(applicationInfo.packageName);
            }
            if (holder.icon != null) {
                Drawable loadIcon = applicationInfo.icon;
                holder.icon.setImageDrawable(loadIcon);
            }
            return convertView;
        }

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					temporarylist=(List<ResolveInfo>)results.values;
					update();
			    }

				@Override
			    protected FilterResults performFiltering(CharSequence constraint) {
			        FilterResults results = new FilterResults();
			        ArrayList<ResolveInfo> FilteredList= new ArrayList<ResolveInfo>();
			        if (constraint == null || constraint.length() == 0) {
			            // No filter implemented we return all the list
			            results.values = mInstalledAppInfo;
			            results.count = mInstalledAppInfo.size();
			        } else {
			        	for (int i = 0; i < mInstalledAppInfo.size(); i++) {
			        		try{
			        			ResolveInfo data = mInstalledAppInfo.get(i);
			        			if (data.loadLabel(mContext.getPackageManager()).toString().toLowerCase()
			        					.contains(constraint.toString().toLowerCase())) {
			        				FilteredList.add(data);
			        			}else if (data.activityInfo.packageName.contains(constraint.toString())) {
			        				FilteredList.add(data);
			        			}
			            	}catch(Exception e){
			            	}
			            }
			            results.values = FilteredList;
			            results.count = FilteredList.size();
			        }
			        return results;
			    }
			};
			return filter;
		}
    }

    static class ViewHolder {
        TextView title;
        ImageView icon;
        TextView subtitle;
    }
    
   
}