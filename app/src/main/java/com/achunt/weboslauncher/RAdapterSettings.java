package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
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

public class RAdapterSettings extends RecyclerView.Adapter<RAdapterSettings.ViewHolder> {

    volatile public static List<AppInfo> appsList;

    public RAdapterSettings(Context c) {

        new Thread(() -> {
            PackageManager pm = c.getPackageManager();
            appsList = new ArrayList<>();
            Intent i = new Intent(Settings.ACTION_SETTINGS);
            List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
            for (ResolveInfo ri : allApps) {
                AppInfo app = new AppInfo();
                app.label = ri.loadLabel(pm);
                app.packageName = ri.activityInfo.packageName;
                app.icon = ri.activityInfo.loadIcon(pm);
                appsList.add(app);
            }

            appsList.sort(Comparator.comparing(o -> o.label.toString()));
        }).start();
    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    public void onBindViewHolder(RAdapterSettings.ViewHolder viewHolder, int i) {
        String appLabel = appsList.get(i).label.toString();
        Drawable appIcon = appsList.get(i).icon;
        TextView textView = viewHolder.textView;
        textView.setText(appLabel);
        ImageView imageView = viewHolder.img;
        imageView.setImageDrawable(appIcon);
    }

    @NonNull
    public RAdapterSettings.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.settings_drawer, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        volatile public TextView textView;
        volatile public ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.settings_name);
            img = itemView.findViewById(R.id.settings_icon);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                Context context = v.getContext();
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appsList.get(pos).packageName.toString());
                context.startActivity(launchIntent);
            });
        }
    }
}