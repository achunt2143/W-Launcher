package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
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
                    // isManagedProfile(UserHandle) isn't a public API; a profile
                    // returned by getUserProfiles() that isn't our own is the work profile.
                    if (!userHandle.equals(android.os.Process.myUserHandle())) {
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
        viewHolder.img.setBackgroundResource(ThemePreference.iconBackgroundRes(viewHolder.itemView.getContext()));

        viewHolder.textView.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.mochilight));
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
                int pos = getAdapterPosition();
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
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || workProfileHandle == null) return false;
                AppActionsMenu.show(v, workProfileApps.get(pos).getApplicationInfo().packageName, workProfileHandle, AppActionsMenu.Source.DRAWER);
                return true;
            });
        }
    }
}
