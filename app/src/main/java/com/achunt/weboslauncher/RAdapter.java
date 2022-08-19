package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RAdapter extends RecyclerView.Adapter<RAdapter.ViewHolder> {

    public static List<AppInfo> appsList;
    public static int phone=0;
    public static int contacts=0;
    public static int messages=0;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        public ImageView img;
        //This is the subclass ViewHolder which simply
        //'holds the views' for us to show on each row
        public ViewHolder(View itemView) {
            super(itemView);
            //Finds the views from our row.xml
            textView = itemView.findViewById(R.id.tv_app_name);
            img = itemView.findViewById(R.id.app_icon);
            itemView.setOnClickListener(this);
        }
       @Override
        public void onClick (View v) {
            int pos = getAdapterPosition();
            Context context = v.getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appsList.get(pos).packageName.toString());
            context.startActivity(launchIntent);
       }
    }



    public RAdapter(Context c)  {
        //This is where we build our list of app details, using the app
        //object we created to store the label, package name and icon
        PackageManager pm = c.getPackageManager();
        appsList = new ArrayList<>();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
        for(ResolveInfo ri:allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            appsList.add(app);
        }
        Collections.sort(appsList, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.label.toString().compareTo(o2.label.toString());
            }
        });
        for (int j = 0; j < appsList.size(); j++){
            if(appsList.get(j).packageName.toString().contains("dialer") || appsList.get(j).packageName.toString().contains("phone")){
                phone = j;
            }
            else if(appsList.get(j).packageName.toString().contains("contacts")){
                contacts = j;
            }
            else if(appsList.get(j).packageName.toString().contains("messag")){
                messages = j;
            }
        }
    }

    public void onBindViewHolder(RAdapter.ViewHolder viewHolder, int i) {
        //Here we use the information in the list we created to define the views
        String appLabel = appsList.get(i).label.toString();
        String appPackage = appsList.get(i).packageName.toString();
        Drawable appIcon = appsList.get(i).icon;
        TextView textView = viewHolder.textView;
        textView.setText(appLabel);
        ImageView imageView = viewHolder.img;
        imageView.setImageDrawable(appIcon);
    }


    public int getItemCount() {
        //This method needs to be overridden so that Androids knows how many items
        //will be making it into the list
        return appsList.size();
    }


    public RAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //This is what adds the code we've written in here to our target view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_row_list_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
}