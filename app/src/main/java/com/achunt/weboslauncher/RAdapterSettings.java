package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RAdapterSettings extends RecyclerView.Adapter<RAdapterSettings.ViewHolder> {

    volatile public static List<AppInfo> appsList;
    volatile Intent i;
    volatile List<ResolveInfo> allApps;
    volatile Intent launchIntent;
    volatile String BT_LABEL;
    volatile String DAT_LABEL;
    volatile String ABT_LABEL;
    volatile String DISP_LABEL;
    volatile String LOC_LABEL;
    volatile String APPS_LABEL;
    volatile String MN_LABEL;
    volatile String NFC_LABEL;
    volatile String PRV_LABEL;
    volatile String SEC_LABEL;
    volatile String SND_LABEL;
    volatile String VPN_LABEL;
    volatile String WIFI_LABEL;
    volatile String WN_LABEL;

    public RAdapterSettings(Context c) {

        new Thread(() -> {
            PackageManager pm = c.getPackageManager();
            appsList = new ArrayList<>();
            SettingsFinder(pm);
            try {
                appsList.sort(Comparator.comparing(o -> o.label.toString()));
            } catch (Exception e) {
                Log.d("Error", String.valueOf(e));
            }
        }).start();
    }

    public void SettingsFinder(PackageManager pm) {
        i = new Intent(Settings.ACTION_SETTINGS);
        //allApps = pm.queryIntentActivities(i, 0);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            BT_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_DATE_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            DAT_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            ABT_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            DISP_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            LOC_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            APPS_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            MN_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_NFC_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            NFC_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            PRV_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            SEC_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_SOUND_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            SND_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_VPN_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            VPN_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_WIFI_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            WIFI_LABEL = app.label.toString();
            appsList.add(app);
        }
        i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo();
            app.label = ri.loadLabel(pm);
            app.packageName = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(pm);
            WN_LABEL = app.label.toString();
            appsList.add(app);
        }
        AppInfo appInfo = new AppInfo();
        appInfo.label = "Help";
        appInfo.packageName = "";
        appsList.add(appInfo);
    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    public void onBindViewHolder(@NonNull RAdapterSettings.ViewHolder viewHolder, int i) {
        String appLabel = appsList.get(i).label.toString();
        Drawable appIcon;
        if (appLabel.contains(BT_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.bt);
        } else if (appLabel.contains(DAT_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.date);
        } else if (appLabel.contains("Help")) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.help);
        } else if (appLabel.contains(DISP_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.lock);
        } else if (appLabel.contains(ABT_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.device);
        } else if (appLabel.contains(LOC_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.gps);
        } else if (appLabel.contains(SND_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.sound);
        } else if (appLabel.contains(VPN_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.vpn);
        } else if (appLabel.contains(WIFI_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.wifi);
        } else if (appLabel.contains(APPS_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.search);
        } else if (appLabel.contains(SEC_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.security);
        } else if (appLabel.contains(PRV_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.privacy);
        } else if (appLabel.contains(WN_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.network);
        } else if (appLabel.contains(NFC_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.nfc);
        } else if (appLabel.contains(MN_LABEL)) {
            appIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.mobilenetwork);
        } else {
            appIcon = appsList.get(i).icon;
        }

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
                Context context = v.getContext();
                int pos = getAdapterPosition();
                String label = appsList.get(pos).label.toString();
                if (label.contentEquals(BT_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(DAT_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_DATE_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(ABT_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(DISP_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(LOC_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(APPS_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(MN_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(NFC_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_NFC_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(PRV_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(SEC_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(SND_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_SOUND_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(VPN_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_VPN_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(WIFI_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals(WN_LABEL)) {
                    launchIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    context.startActivity(launchIntent);
                } else if (label.contentEquals("Help")) {
                    AppCompatActivity activity = (AppCompatActivity) itemView.getContext();
                    Fragment myFragment = new HelpPage();
                    myFragment.setExitTransition(new Slide(Gravity.TOP));
                    myFragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
                } else {
                    launchIntent = new Intent(Settings.ACTION_SETTINGS);
                    context.startActivity(launchIntent);
                }
            });
        }
    }

}