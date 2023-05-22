package com.achunt.weboslauncher;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

public class RAdapter extends RecyclerView.Adapter<RAdapter.ViewHolder> {

    private final int PERSONAL_WORK_PROFILE_USER_ID = 999;
    List<AppInfo> appsList;
    List<ResolveInfo> allApps;
    int phone = 0;
    int contacts = 0;
    int messages = 0;

    public RAdapter(Context c) {
        appsList = new ArrayList<>();

        PackageManager pm = c.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        allApps = pm.queryIntentActivities(i, 0);
        try {
            for (ResolveInfo ri : allApps) {
                AppInfo app = new AppInfo();
                app.label = ri.loadLabel(pm);
                app.packageName = ri.activityInfo.packageName;
                app.icon = ri.activityInfo.loadIcon(pm);
                appsList.add(app);
            }
            sortAppsList();
            findSpecialApps(c);
        } catch (Exception e) {
            Log.d("Error", String.valueOf(e));
        }
    }

    public void sortAppsList() {
        try {
            appsList.sort(Comparator.comparing(appInfo -> appInfo.label.toString()));
        } catch (Exception e) {
            Log.e("Error", "Sorting error: " + e.getMessage());
        }
    }

    private void findSpecialApps(Context c) {
        SharedPreferences sharedPrefs = c.getSharedPreferences("SpecialApps", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        for (int j = 0; j < appsList.size(); j++) {
            String packageName = appsList.get(j).packageName.toString();
            if (packageName.matches("^com.*.android.*.dialer$")) {
                phone = j;
                editor.putString("PhonePackageName", packageName);
            } else if (packageName.matches("^com.*.android.*.contacts$")) {
                contacts = j;
                editor.putString("ContactsPackageName", packageName);
            } else if (packageName.matches("^com.*.android.*.messaging$") || packageName.matches("^com.*.android.*.mms$")) {
                messages = j;
                editor.putString("MessagesPackageName", packageName);
            }
        }
        editor.apply();
    }


    @Override
    public int getItemCount() {
        return appsList.size();
    }

    public List<AppInfo> getAppsList() {
        return appsList;
    }

    public List<ResolveInfo> getResolveList() {
        return allApps;
    }

    public void onBindViewHolder(RAdapter.ViewHolder viewHolder, int i) {
        AppInfo appInfo = appsList.get(i);
        viewHolder.textView.setText(appInfo.label.toString());
        viewHolder.img.setImageDrawable(appInfo.icon);
    }

    @NonNull
    public RAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
                if (pos != RecyclerView.NO_POSITION) {
                    Context context = v.getContext();
                    String packageName = appsList.get(pos).packageName.toString();
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        context.startActivity(launchIntent);
                    }
                }
            });
        }
    }


}