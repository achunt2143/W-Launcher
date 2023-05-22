package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

public class RAdapterSystem extends RecyclerView.Adapter<RAdapterSystem.ViewHolder> {

    public static List<AppInfo> appsListS;

    public RAdapterSystem(Context context, List<ResolveInfo> allApps) {
        appsListS = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();

        for (ResolveInfo resolveInfo : allApps) {
            ApplicationInfo applicationInfo;
            try {
                applicationInfo = packageManager.getApplicationInfo(resolveInfo.activityInfo.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                continue;
            }

            if (applicationInfo.sourceDir.startsWith("/system") ||
                    (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 ||
                    (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                appsListS.add(createAppInfo(packageManager, resolveInfo));
            }
        }

        appsListS.sort(Comparator.comparing(appInfo -> appInfo.label.toString()));
    }

    private AppInfo createAppInfo(PackageManager packageManager, ResolveInfo resolveInfo) {
        AppInfo appInfo = new AppInfo();
        appInfo.label = resolveInfo.loadLabel(packageManager);
        appInfo.packageName = resolveInfo.activityInfo.packageName;
        appInfo.icon = resolveInfo.activityInfo.loadIcon(packageManager);
        return appInfo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_row_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        AppInfo appInfo = appsListS.get(position);

        TextView textView = viewHolder.textView;
        ImageView imageView = viewHolder.img;

        textView.setText(appInfo.label);
        imageView.setImageDrawable(appInfo.icon);

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
        textView.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return appsListS.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_app_name);
            img = itemView.findViewById(R.id.app_icon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Context context = v.getContext();
                    String packageName = (String) appsListS.get(position).packageName;
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        context.startActivity(launchIntent);
                    }
                }
            });
        }
    }
}