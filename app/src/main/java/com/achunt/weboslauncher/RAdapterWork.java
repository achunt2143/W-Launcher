package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RAdapterWork extends RecyclerView.Adapter<RAdapterWork.ViewHolder> {

    List<ApplicationInfo> workProfileApps;

    public RAdapterWork(Context c) {
        PackageManager pm = c.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        UserManager userManager = (UserManager) c.getSystemService(Context.USER_SERVICE);

        List<UserHandle> userProfiles = userManager.getUserProfiles();

        for (UserHandle userHandle : userProfiles) {
            int userId = (int) userManager.getSerialNumberForUser(userHandle);

            boolean isManagedProfile = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                isManagedProfile = userManager.isManagedProfile();
            }
            boolean isUserRunning = userManager.isUserRunning(userHandle);

            if (isManagedProfile && isUserRunning) {
                List<ApplicationInfo> installedApps = pm.getInstalledApplications(
                        PackageManager.GET_META_DATA);

                for (ApplicationInfo appInfo : installedApps) {
                    // Check if the app belongs to the work profile
                    if (appInfo.packageName.equals(userId + ":" + appInfo.packageName)) {
                        workProfileApps.add(appInfo);
                    }
                }
            }
        }

    }


    @Override
    public int getItemCount() {
        return workProfileApps.size();
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String appLabel = workProfileApps.get(i).processName;
        int appIcon = workProfileApps.get(i).icon;
        TextView textView = viewHolder.textView;
        textView.setText(appLabel);
        ImageView imageView = viewHolder.img;
        imageView.setImageResource(appIcon);
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_row_list_view, parent, false);
        return new ViewHolder(view);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        volatile public TextView textView;
        volatile public ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_app_name);
            img = itemView.findViewById(R.id.app_icon);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                Context context = v.getContext();
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(workProfileApps.get(pos).packageName);
                context.startActivity(launchIntent);
            });
        }
    }
}
