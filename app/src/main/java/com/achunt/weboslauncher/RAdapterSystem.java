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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RAdapterSystem extends RecyclerView.Adapter<RAdapterSystem.ViewHolder> {

    volatile public static List<AppInfo> appsListS;
    public static int phone = 0;
    public static int contacts = 0;
    public static int messages = 0;

    public RAdapterSystem(Context c) {

        new Thread(() -> {
            PackageManager pm = c.getPackageManager();
            appsListS = new ArrayList<>();
            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);

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
                if (ai.sourceDir.startsWith("/system") || (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 || (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    appsListS.add(app);
                }
            }
            appsListS.sort(Comparator.comparing(o -> o.label.toString()));

        }).start();
    }

    @Override
    public int getItemCount() {
        return appsListS.size();
    }

    public void onBindViewHolder(RAdapterSystem.ViewHolder viewHolder, int i) {
        String appLabel = appsListS.get(i).label.toString();
        Drawable appIcon = appsListS.get(i).icon;
        TextView textView = viewHolder.textView;
        textView.setText(appLabel);
        ImageView imageView = viewHolder.img;
        imageView.setImageDrawable(appIcon);
    }

    @NonNull
    public RAdapterSystem.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

            /*new Thread(() -> */
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                Context context = v.getContext();
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appsListS.get(pos).packageName.toString());
                context.startActivity(launchIntent);
            });/*).start();*/
        }
    }
}