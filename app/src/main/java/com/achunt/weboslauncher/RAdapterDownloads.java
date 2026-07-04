package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RAdapterDownloads extends RecyclerView.Adapter<RAdapterDownloads.ViewHolder> {

    private static final HashSet<String> systemAppPackageNames = new HashSet<>();

    private final List<AppInfo> appsListD;

    public RAdapterDownloads(Context context, List<ResolveInfo> allApps) {
        appsListD = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();

        for (AppInfo appInfo : RAdapterSystem.appsListS) {
            systemAppPackageNames.add(appInfo.packageName.toString());
        }

        for (ResolveInfo resolveInfo : allApps) {
            String packageName = resolveInfo.activityInfo.packageName;
            ApplicationInfo applicationInfo;
            try {
                applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                continue;
            }

            if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 &&
                    (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appsListD.add(createAppInfo(packageManager, resolveInfo));
            }
        }

        appsListD.sort(Comparator.comparing(appInfo -> appInfo.label.toString()));
    }

    private AppInfo createAppInfo(PackageManager packageManager, ResolveInfo resolveInfo) {
        AppInfo appInfo = new AppInfo();
        appInfo.label = resolveInfo.loadLabel(packageManager);
        appInfo.packageName = resolveInfo.activityInfo.packageName;
        appInfo.icon = resolveInfo.activityInfo.loadIcon(packageManager);
        return appInfo;
    }

    @Override
    public int getItemCount() {
        return appsListD.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RAdapterDownloads.ViewHolder viewHolder, int i) {
        String appLabel = appsListD.get(i).label.toString();
        Drawable appIcon = appsListD.get(i).icon;
        TextView textView = viewHolder.textView;
        textView.setText(appLabel);
        ImageView imageView = viewHolder.img;
        imageView.setImageDrawable(appIcon);
        imageView.setBackgroundResource(ThemePreference.iconBackgroundRes(viewHolder.itemView.getContext()));
        textView.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.mochilight));
    }

    @NonNull
    @Override
    public RAdapterDownloads.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
                if (pos != RecyclerView.NO_POSITION) {
                    Context context = v.getContext();
                    String packageName = appsListD.get(pos).packageName.toString();
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        Set<String> gbl = HomeScreenK.Companion.getGoodbyeList();
                        gbl.remove(packageName);
                        HomeScreenK.Companion.setGoodbyeList(gbl);
                        context.startActivity(launchIntent);
                    }
                }
            });
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return false;
                AppActionsMenu.show(v, appsListD.get(pos).packageName.toString(), AppActionsMenu.Source.DRAWER);
                return true;
            });
        }
    }
}
