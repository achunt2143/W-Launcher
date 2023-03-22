package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RAdapterDownloads extends RecyclerView.Adapter<RAdapterDownloads.ViewHolder> {

    volatile public static List<AppInfo> appsListD;

    public RAdapterDownloads(Context c, List<ResolveInfo> allApps) {


        PackageManager pm = c.getPackageManager();
        appsListD = new ArrayList<>();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        //List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);

        for (ResolveInfo ri : allApps) {
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo(ri.activityInfo.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                AppInfo app = new AppInfo();
                app.label = ri.loadLabel(pm);
                app.packageName = ri.activityInfo.packageName;
                app.icon = ri.activityInfo.loadIcon(pm);
                if ((ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 || (ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appsListD.add(app);
                }
            }
            for (int j = 0; j < RAdapterSystem.appsListS.size(); j++) {
                for (int k = 0; k < appsListD.size(); k++) {
                    if (appsListD.get(k).packageName.equals(RAdapterSystem.appsListS.get(j).packageName)) {
                        appsListD.remove(k);
                    }
                }
            }
            try {
                appsListD.sort(Comparator.comparing(o -> o.label.toString()));
            } catch (Exception e) {
                Log.d("Error", String.valueOf(e));
            }

    }

    @Override
    public int getItemCount() {
        return appsListD.size();
    }

    public void onBindViewHolder(RAdapterDownloads.ViewHolder viewHolder, int i) {
        String appLabel = appsListD.get(i).label.toString();
        Drawable appIcon = appsListD.get(i).icon;
        TextView textView = viewHolder.textView;
        textView.setText(appLabel);
        ImageView imageView = viewHolder.img;
        imageView.setImageDrawable(appIcon);
        SharedPreferences sharedPref = viewHolder.itemView.getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String theme = sharedPref.getString("themeName", "Classic");
        switch (theme) {
            case "Classic":  //classic
            case "Modern":  //modern
                textView.setTextColor(viewHolder.itemView.getResources().getColor(R.color.mochilight));
                break;
            case "Mochi":  //mochi
                textView.setTextColor(viewHolder.itemView.getResources().getColor(R.color.mochigrey));
                break;
            case "System":  //system
                textView.setTextColor(viewHolder.itemView.getResources().getColor(R.color.white));
                break;
        }
    }

    @NonNull
    public RAdapterDownloads.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_row_list_view, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        volatile public TextView textView;
        volatile public ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_app_name);
            img = itemView.findViewById(R.id.app_icon);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                Context context = v.getContext();
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appsListD.get(pos).packageName.toString());
                context.startActivity(launchIntent);
            });
        }
    }
}