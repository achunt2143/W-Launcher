package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class RAdapterWork extends RecyclerView.Adapter<RAdapterWork.ViewHolder> {

    private final List<LauncherActivityInfo> workProfileApps;
    private final UserHandle workProfileHandle;
    private final LauncherApps launcherApps;

    public RAdapterWork(Context c) {
        workProfileApps = new ArrayList<>();
        launcherApps = (LauncherApps) c.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager userManager = (UserManager) c.getSystemService(Context.USER_SERVICE);
        UserHandle foundHandle = null;

        if (userManager != null && launcherApps != null) {
            List<UserHandle> userProfiles = userManager.getUserProfiles();
            for (UserHandle userHandle : userProfiles) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (userManager.isManagedProfile(userHandle)) {
                        foundHandle = userHandle;
                        try {
                            List<LauncherActivityInfo> activities = launcherApps.getActivityList(null, userHandle);
                            workProfileApps.addAll(activities);
                        } catch (Exception e) {
                            Log.e("RAdapterWork", "Error loading work profile apps: " + e.getMessage(), e);
                        }
                        break;
                    }
                }
            }
        }

        workProfileHandle = foundHandle;
        workProfileApps.sort(Comparator.comparing(info -> info.getLabel().toString()));
    }

    @Override
    public int getItemCount() {
        return workProfileApps.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        LauncherActivityInfo appInfo = workProfileApps.get(i);
        String appLabel = appInfo.getLabel().toString();
        Drawable appIcon = appInfo.getIcon(0);

        viewHolder.textView.setText(appLabel);
        viewHolder.img.setImageDrawable(appIcon);

        SharedPreferences sharedPref = viewHolder.itemView.getContext()
                .getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String theme = sharedPref.getString("themeName", "Classic");
        int textColor;
        switch (theme) {
            case "Classic":
            case "Modern":
                textColor = ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.mochilight);
                break;
            case "Mochi":
                textColor = ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.mochigrey);
                break;
            case "System":
                textColor = ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.white);
                break;
            default:
                textColor = ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.mochilight);
                break;
        }
        viewHolder.textView.setTextColor(textColor);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_row_list_view, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_app_name);
            img = itemView.findViewById(R.id.app_icon);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && workProfileHandle != null && launcherApps != null) {
                    LauncherActivityInfo appInfo = workProfileApps.get(pos);
                    try {
                        Set<String> gbl = HomeScreenK.Companion.getGoodbyeList();
                        gbl.remove(appInfo.getApplicationInfo().packageName);
                        HomeScreenK.Companion.setGoodbyeList(gbl);
                        launcherApps.startMainActivity(
                                appInfo.getComponentName(),
                                workProfileHandle,
                                v.getClipBounds(),
                                null
                        );
                    } catch (Exception e) {
                        Log.e("RAdapterWork", "Failed to launch work app: " + e.getMessage(), e);
                    }
                }
            });
        }
    }
}
