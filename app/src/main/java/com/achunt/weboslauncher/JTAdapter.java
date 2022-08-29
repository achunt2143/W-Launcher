package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
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

public class JTAdapter extends RecyclerView.Adapter<JTAdapter.ViewHolder> {

    public static List<AppInfo> jtList;

    public JTAdapter(Context c, String q) {
        new Thread(() -> {
            PackageManager pm = c.getPackageManager();
            jtList = new ArrayList<>();
            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
            for (ResolveInfo ri : allApps) {
                AppInfo app = new AppInfo();
                app.label = ri.loadLabel(pm);
                app.packageName = ri.activityInfo.packageName;
                app.icon = ri.activityInfo.loadIcon(pm);
                if (app.label.toString().toLowerCase().startsWith(q)) {
                    jtList.add(app);
                }
            }
            jtList.sort(Comparator.comparing(o -> o.label.toString()));
        }).start();
    }

    @Override
    public int getItemCount() {
        return jtList.size();
    }

    public void onBindViewHolder(JTAdapter.ViewHolder viewHolder, int i) {
        try {
            String appLabel = jtList.get(i).label.toString();
            Drawable appIcon = jtList.get(i).icon;
            TextView textView = viewHolder.textView;
            textView.setText(appLabel);
            ImageView imageView = viewHolder.img;
            imageView.setImageDrawable(appIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @NonNull
    public JTAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.jt_item_row_layout, parent, false);
        return new JTAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView img;

        public ViewHolder(View itemView) {

            super(itemView);
            textView = itemView.findViewById(R.id.jt_app_name);
            img = itemView.findViewById(R.id.jt_app_icon);

            /*new Thread(() -> */
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                Context context = v.getContext();
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(jtList.get(pos).packageName.toString());
                context.startActivity(launchIntent);
            });/*).start()*/
        }
    }
}
