package com.achunt.weboslauncher;

import android.animation.ValueAnimator;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.Settings;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private HomeScreenK homeScreenFragment;
    private FrameLayout notificationContainerFrame = null;
    private FrameLayout notificationContainerFrameClickArea = null;
    private boolean isExpanded = false;

    private static final String TAG = "MainActivity";

    public static List<UsageStats> getUsageStatsList(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DATE, -1);
        long startTime = calendar.getTimeInMillis();

        return usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window w = getWindow();
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.empty));

        if (!isUsageAccessGranted()) {
            Toast.makeText(this, "Please allow Usage Access", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            checkNotificationListenerPermission();
        }

        loadFragment(new HomeScreenK());

        if (checkNotificationEnabled()) {
            checkNotificationListenerPermission();
            loadNotificationFragment(new NotificationFragment());
            notificationContainerFrame = findViewById(R.id.notificationContainer);
            notificationContainerFrameClickArea = findViewById(R.id.notificationContainerClickArea);
            notificationContainerFrame.setOnClickListener(view -> toggleNotification());
            notificationContainerFrameClickArea.setOnClickListener(view -> toggleNotification());
        }

        // Replace deprecated onBackPressed() override with OnBackPressedCallback
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 2) {
                    if (fm.findFragmentByTag("apps") != null) {
                        fm.popBackStack("apps", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                    Window w = getWindow();
                    w.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.empty));
                    LinearLayout widgets = findViewById(R.id.widgets);
                    widgets.animate().alpha(1).setDuration(1000).start();
                }
                // If back stack count <= 2, let the system handle it (minimise/exit)
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });
    }

    private void toggleNotification() {
        if (checkNotificationEnabled()) {
            int targetHeight = isExpanded
                    ? getResources().getDimensionPixelSize(R.dimen.notification_height_collapsed)
                    : getResources().getDimensionPixelSize(R.dimen.notification_height_expanded);

            ValueAnimator animation = ValueAnimator.ofInt(notificationContainerFrame.getHeight(), targetHeight);
            animation.addUpdateListener(valueAnimator -> {
                int value = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = notificationContainerFrame.getLayoutParams();
                layoutParams.height = value;
                notificationContainerFrame.setLayoutParams(layoutParams);
                ViewGroup.LayoutParams layoutParamsHelper = notificationContainerFrameClickArea.getLayoutParams();
                layoutParamsHelper.height = targetHeight;
                notificationContainerFrameClickArea.setLayoutParams(layoutParamsHelper);
            });
            animation.setDuration(400);
            animation.start();

            isExpanded = !isExpanded;
            Fragment notifFragment = getSupportFragmentManager().findFragmentByTag("notifications");
            if (notifFragment instanceof NotificationFragment) {
                ((NotificationFragment) notifFragment).refreshView();
            }
        }
    }

    public boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            fragment.setRetainInstance(true);
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            fragment.setExitTransition(new Slide(Gravity.BOTTOM));
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, "home")
                    .setReorderingAllowed(true)
                    .addToBackStack("main")
                    .commit();
            if (fragment instanceof HomeScreenK) {
                homeScreenFragment = (HomeScreenK) fragment;
            } else {
                homeScreenFragment = null;
            }
            return true;
        }
        return false;
    }

    public boolean loadNotificationFragment(Fragment fragment) {
        if (fragment != null) {
            fragment.setRetainInstance(true);
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            fragment.setExitTransition(new Slide(Gravity.BOTTOM));
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.notificationContainer, fragment, "notifications")
                    .setReorderingAllowed(true)
                    .addToBackStack("main")
                    .commit();
            if (fragment instanceof NotificationFragment) {
                ((NotificationFragment) fragment).setOnNotificationsReadyListener(this::checkAndSetNotificationVisibility);
            }
            return true;
        }
        return false;
    }

    public void checkAndSetNotificationVisibility() {
        if (checkNotificationEnabled()) {
            Fragment notifFragment = getSupportFragmentManager().findFragmentByTag("notifications");
            if (notifFragment instanceof NotificationFragment) {
                boolean hasNotifs = ((NotificationFragment) notifFragment).hasNotifications();
                notificationContainerFrame.setVisibility(hasNotifs ? View.VISIBLE : View.GONE);
                notificationContainerFrameClickArea.setVisibility(hasNotifs ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void checkPermission(String permission, int requestCode) {
        requestPermissions(new String[]{permission}, requestCode);
    }

    public boolean checkNotificationEnabled() {
        SharedPreferences sharedPrefH = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPrefH.getBoolean("notifications", false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 69420) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Please allow Usage Access", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            LinearLayout widgets = findViewById(R.id.widgets);
            widgets.animate().alpha(1).setDuration(1000).start();
        } catch (Exception ignored) {
        }
        Window w = getWindow();
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.empty));

        if (homeScreenFragment != null) {
            homeScreenFragment.recentsList(getApplicationContext());
        }
        if (checkNotificationEnabled()) {
            checkAndSetNotificationVisibility();
        }
    }

    private boolean isUsageAccessGranted() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat != null && !flat.isEmpty()) {
            final String[] names = flat.split(":");
            for (String name : names) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && pkgName.equals(cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkNotificationListenerPermission() {
        if (!isNotificationServiceEnabled()) {
            Log.d(TAG, "Notification listener not enabled, launching settings");
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
