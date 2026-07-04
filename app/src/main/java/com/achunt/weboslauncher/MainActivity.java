package com.achunt.weboslauncher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
    private FrameLayout container = null;
    private View notificationGapFill = null;
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

        container = findViewById(R.id.container);
        notificationGapFill = findViewById(R.id.notificationGapFill);

        if (checkNotificationEnabled()) {
            checkNotificationListenerPermission();
            loadNotificationFragment(new NotificationFragment());
            notificationContainerFrame = findViewById(R.id.notificationContainer);
            notificationContainerFrame.setOnClickListener(view -> toggleNotification());
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

    public void toggleNotification() {
        Log.d(TAG, "toggleNotification() called, notifEnabled=" + checkNotificationEnabled() + " isExpanded=" + isExpanded);
        if (!checkNotificationEnabled()) return;
        setExpanded(!isExpanded);
    }

    /** Ensures the panel is expanded — used when tapping a specific app's icon in the collapsed row. */
    public void expandNotificationPanel() {
        Log.d(TAG, "expandNotificationPanel() called, notifEnabled=" + checkNotificationEnabled() + " isExpanded=" + isExpanded);
        if (!checkNotificationEnabled() || isExpanded) return;
        setExpanded(true);
    }

    /** Ensures the panel is collapsed — used for the reserved swipe-down-on-a-card gesture. */
    public void collapseNotificationPanel() {
        if (!checkNotificationEnabled() || !isExpanded) return;
        setExpanded(false);
    }

    private void setExpanded(boolean expanded) {
        Log.d(TAG, "setExpanded(" + expanded + ")");
        isExpanded = expanded;
        Fragment notifFragment = getSupportFragmentManager().findFragmentByTag("notifications");
        if (notifFragment instanceof NotificationFragment) {
            ((NotificationFragment) notifFragment).applyExpandedState(isExpanded);
        }
        animatePanelToTargetHeight();
    }

    /**
     * Recomputes and animates the panel to whatever height its current content
     * (collapsed icon row, or the full expanded list) actually needs — capped at
     * 50% of the screen by MaxHeightFrameLayout regardless of what we compute here.
     * Called on every expand/collapse toggle, and again whenever the notification
     * list itself changes size while already expanded, so the panel keeps growing
     * or shrinking to match — never taller than the current content demands.
     */
    private void animatePanelToTargetHeight() {
        int targetHeight = isExpanded
                ? computeExpandedTargetHeight()
                : getResources().getDimensionPixelSize(R.dimen.notification_height_collapsed);

        ValueAnimator animation = ValueAnimator.ofInt(notificationContainerFrame.getHeight(), targetHeight);
        animation.addUpdateListener(valueAnimator -> {
            int value = (int) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = notificationContainerFrame.getLayoutParams();
            layoutParams.height = value;
            notificationContainerFrame.setLayoutParams(layoutParams);
        });
        animation.setDuration(300);
        animation.start();
    }

    private int computeExpandedTargetHeight() {
        Fragment notifFragment = getSupportFragmentManager().findFragmentByTag("notifications");
        int fallbackHeight = getResources().getDimensionPixelSize(R.dimen.notification_height_collapsed);
        if (!(notifFragment instanceof NotificationFragment) || notifFragment.getView() == null) {
            Log.d(TAG, "computeExpandedTargetHeight: no content view, using fallback " + fallbackHeight);
            return fallbackHeight;
        }

        int width = notificationContainerFrame.getWidth();
        if (width <= 0) width = getResources().getDisplayMetrics().widthPixels;

        // Measures the expanded list alone — the collapsed icon row is still VISIBLE alongside
        // it for the cross-slide, so measuring the shared content view directly here would sum
        // both rows' heights into an inflated target.
        int measured = ((NotificationFragment) notifFragment).computeTargetHeight(true, width);
        Log.d(TAG, "computeExpandedTargetHeight: width=" + width + " measured=" + measured
                + " containerCurrentHeight=" + notificationContainerFrame.getHeight());
        return measured;
    }

    /** Called by NotificationFragment whenever the notification list changes size. */
    public void refreshNotificationPanelHeight() {
        if (notificationContainerFrame == null || !isExpanded) return;
        notificationContainerFrame.post(this::animatePanelToTargetHeight);
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
        if (!checkNotificationEnabled() || notificationContainerFrame == null) return;
        Fragment notifFragment = getSupportFragmentManager().findFragmentByTag("notifications");
        if (!(notifFragment instanceof NotificationFragment)) return;

        boolean hasNotifs = ((NotificationFragment) notifFragment).hasNotifications();
        boolean currentlyVisible = notificationContainerFrame.getVisibility() == View.VISIBLE;
        if (hasNotifs == currentlyVisible) return;

        if (hasNotifs) {
            showNotificationPanel();
        } else {
            hideNotificationPanel();
        }
    }

    /**
     * Slides the panel up into view (height animates 0 → collapsed, which for a bottom-anchored
     * view reads as sliding up from the screen edge), with a small black gap opening up above
     * the panel's own rounded top corners.
     */
    private void showNotificationPanel() {
        ViewGroup.MarginLayoutParams containerParams = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
        containerParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.notification_panel_gap);
        container.setLayoutParams(containerParams);
        notificationGapFill.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams params = notificationContainerFrame.getLayoutParams();
        params.height = 0;
        notificationContainerFrame.setLayoutParams(params);
        notificationContainerFrame.setVisibility(View.VISIBLE);

        int collapsedHeight = getResources().getDimensionPixelSize(R.dimen.notification_height_collapsed);
        ValueAnimator animation = ValueAnimator.ofInt(0, collapsedHeight);
        animation.addUpdateListener(va -> {
            ViewGroup.LayoutParams lp = notificationContainerFrame.getLayoutParams();
            lp.height = (int) va.getAnimatedValue();
            notificationContainerFrame.setLayoutParams(lp);
        });
        animation.setDuration(300);
        animation.start();
    }

    /** Slides the panel down out of view, then closes the gap. */
    private void hideNotificationPanel() {
        ValueAnimator animation = ValueAnimator.ofInt(notificationContainerFrame.getHeight(), 0);
        animation.addUpdateListener(va -> {
            ViewGroup.LayoutParams lp = notificationContainerFrame.getLayoutParams();
            lp.height = (int) va.getAnimatedValue();
            notificationContainerFrame.setLayoutParams(lp);
        });
        animation.setDuration(300);
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                notificationContainerFrame.setVisibility(View.GONE);
                isExpanded = false;
                Fragment notifFragment = getSupportFragmentManager().findFragmentByTag("notifications");
                if (notifFragment instanceof NotificationFragment) {
                    // Reset to the collapsed icon row so the next notification to arrive
                    // shows in the preview area rather than reopening the expanded list.
                    ((NotificationFragment) notifFragment).applyExpandedState(false, true);
                }

                notificationGapFill.setVisibility(View.GONE);
                ViewGroup.MarginLayoutParams containerParams = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
                containerParams.bottomMargin = 0;
                container.setLayoutParams(containerParams);
            }
        });
        animation.start();
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
