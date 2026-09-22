package com.achunt.weboslauncher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private HomeScreenK homeScreenFragment;
    private FrameLayout notificationContainerFrame = null;
    private FrameLayout container = null;
    private View notificationGapFill = null;
    private View statusBarShadow = null;
    private boolean isExpanded = false;
    private int currentTopInset = 0;
    private int currentBottomInset = 0;
    private ValueAnimator panelHeightAnimator = null;

    private static final String TAG = "MainActivity";

    private void initWindowInsets() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.view.WindowManager wm = getSystemService(android.view.WindowManager.class);
            if (wm != null) {
                try {
                    android.view.WindowMetrics metrics = wm.getCurrentWindowMetrics();
                    android.view.WindowInsets windowInsets = metrics.getWindowInsets();
                    android.graphics.Insets insets = windowInsets.getInsets(
                            android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout()
                    );
                    android.graphics.Insets gestureInsets = windowInsets.getInsets(
                            android.view.WindowInsets.Type.mandatorySystemGestures()
                    );
                    if (insets.top > 0) currentTopInset = insets.top;
                    int bottom = Math.max(insets.bottom, gestureInsets.bottom);
                    if (bottom > 0) currentBottomInset = bottom;
                } catch (Exception e) {
                    Log.w(TAG, "Failed to get insets from WindowMetrics", e);
                }
            }
        }
        if (currentBottomInset == 0) {
            int navBarResId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResId > 0) {
                currentBottomInset = getResources().getDimensionPixelSize(navBarResId);
            }
        }
        if (currentTopInset == 0) {
            int statusBarResId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarResId > 0) {
                currentTopInset = getResources().getDimensionPixelSize(statusBarResId);
            }
        }
    }

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

        initWindowInsets();

        Window w = getWindow();
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.empty));

        if (!isUsageAccessGranted()) {
            Toast.makeText(this, "Please allow Usage Access", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            checkNotificationListenerPermission();
        }

        container = findViewById(R.id.container);
        notificationGapFill = findViewById(R.id.notificationGapFill);
        statusBarShadow = findViewById(R.id.statusBarShadow);

        if (container != null && currentTopInset > 0) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
            if (lp.topMargin != currentTopInset) {
                lp.topMargin = currentTopInset;
                container.setLayoutParams(lp);
            }
        }
        if (statusBarShadow != null && currentTopInset > 0) {
            int shadowBleed = (int) (16 * getResources().getDisplayMetrics().density);
            ViewGroup.LayoutParams slp = statusBarShadow.getLayoutParams();
            if (slp.height != currentTopInset + shadowBleed) {
                slp.height = currentTopInset + shadowBleed;
                statusBarShadow.setLayoutParams(slp);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            Insets gestureInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.mandatorySystemGestures()
            );
            currentTopInset = insets.top;
            currentBottomInset = Math.max(insets.bottom, gestureInsets.bottom);

            if (container != null) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
                if (lp.topMargin != currentTopInset) {
                    lp.topMargin = currentTopInset;
                    container.setLayoutParams(lp);
                }
            }
            if (statusBarShadow != null) {
                int shadowBleed = (int) (16 * getResources().getDisplayMetrics().density);
                ViewGroup.LayoutParams slp = statusBarShadow.getLayoutParams();
                if (slp.height != currentTopInset + shadowBleed) {
                    slp.height = currentTopInset + shadowBleed;
                    statusBarShadow.setLayoutParams(slp);
                }
            }
            updateBottomPadding();
            return windowInsets;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateStatusBarShadow);

        loadFragment(new HomeScreenK());

        if (checkNotificationEnabled()) {
            checkNotificationListenerPermission();
            loadNotificationFragment(new NotificationFragment());
            notificationContainerFrame = findViewById(R.id.notificationContainer);
            notificationContainerFrame.setOnClickListener(view -> toggleNotification());
        }

        // OnBackPressedCallback: progressively closes open sub-surfaces without closing the main UI
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 1. If notification panel is expanded, collapse it first
                if (isExpanded) {
                    collapseNotificationPanel();
                    return;
                }

                FragmentManager fm = getSupportFragmentManager();

                // 2. If AppsDrawer is open, pop it and restore home screen widgets
                if (homeScreenFragment != null && homeScreenFragment.isAdded() && homeScreenFragment.closeAppsDrawer()) {
                    Window w = getWindow();
                    w.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.empty));
                    setStatusBarShadowVisible(false);
                    LinearLayout widgets = findViewById(R.id.widgets);
                    if (widgets != null) {
                        widgets.animate().alpha(1).setDuration(1000).start();
                    }
                    return;
                }
                if (fm.findFragmentByTag("apps") != null) {
                    fm.popBackStack("apps", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    Window w = getWindow();
                    w.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.empty));
                    setStatusBarShadowVisible(false);
                    LinearLayout widgets = findViewById(R.id.widgets);
                    if (widgets != null) {
                        widgets.animate().alpha(1).setDuration(1000).start();
                    }
                    return;
                }

                // 3. If any back stack entry exists (e.g. HelpPage / CreditsPage)
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    return;
                }

                // 4. If currently showing HelpPage or CreditsPage in container without a backstack entry
                Fragment currentFragment = fm.findFragmentById(R.id.container);
                if (currentFragment instanceof HelpPage || currentFragment instanceof CreditsPage) {
                    loadFragment(new HomeScreenK());
                    return;
                }

                // 5. On the root home screen: do nothing so the launcher main UI stays permanently
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

    public void updateBottomPadding() {
        boolean notifVisible = notificationContainerFrame != null && notificationContainerFrame.getVisibility() == View.VISIBLE;
        if (notifVisible) {
            notificationContainerFrame.setPadding(
                    notificationContainerFrame.getPaddingLeft(),
                    notificationContainerFrame.getPaddingTop(),
                    notificationContainerFrame.getPaddingRight(),
                    currentBottomInset
            );
            if (!isExpanded) {
                int targetHeight = getResources().getDimensionPixelSize(R.dimen.notification_height_collapsed) + currentBottomInset;
                if (panelHeightAnimator != null && panelHeightAnimator.isRunning()) {
                    panelHeightAnimator.cancel();
                    ViewGroup.LayoutParams lp = notificationContainerFrame.getLayoutParams();
                    int startH = lp != null ? lp.height : 0;
                    panelHeightAnimator = ValueAnimator.ofInt(startH, targetHeight);
                    panelHeightAnimator.addUpdateListener(va -> {
                        ViewGroup.LayoutParams p = notificationContainerFrame.getLayoutParams();
                        p.height = (int) va.getAnimatedValue();
                        notificationContainerFrame.setLayoutParams(p);
                    });
                    panelHeightAnimator.setDuration(200);
                    panelHeightAnimator.start();
                } else {
                    ViewGroup.LayoutParams lp = notificationContainerFrame.getLayoutParams();
                    if (lp != null && lp.height != targetHeight && lp.height > 0) {
                        lp.height = targetHeight;
                        notificationContainerFrame.setLayoutParams(lp);
                    }
                }
            } else {
                int targetHeight = computeExpandedTargetHeight();
                if (panelHeightAnimator == null || !panelHeightAnimator.isRunning()) {
                    ViewGroup.LayoutParams lp = notificationContainerFrame.getLayoutParams();
                    if (lp != null && lp.height != targetHeight && lp.height > 0) {
                        lp.height = targetHeight;
                        notificationContainerFrame.setLayoutParams(lp);
                    }
                }
            }
            if (homeScreenFragment != null) {
                homeScreenFragment.updateDockBottomPadding(0);
            }
        } else {
            if (notificationContainerFrame != null) {
                notificationContainerFrame.setPadding(
                        notificationContainerFrame.getPaddingLeft(),
                        notificationContainerFrame.getPaddingTop(),
                        notificationContainerFrame.getPaddingRight(),
                        0
                );
            }
            if (homeScreenFragment != null) {
                homeScreenFragment.updateDockBottomPadding(currentBottomInset);
            }
        }
    }

    public int getDockBottomPadding() {
        boolean notifVisible = notificationContainerFrame != null && notificationContainerFrame.getVisibility() == View.VISIBLE;
        return notifVisible ? 0 : currentBottomInset;
    }

    public int getCurrentBottomInset() {
        return currentBottomInset;
    }

    public void setStatusBarShadowVisible(boolean visible) {
        if (statusBarShadow == null) return;
        if (visible) {
            if (statusBarShadow.getVisibility() != View.VISIBLE) {
                statusBarShadow.setAlpha(0f);
                statusBarShadow.setVisibility(View.VISIBLE);
            }
            statusBarShadow.animate().cancel();
            statusBarShadow.animate().alpha(1f).setDuration(250).start();
        } else {
            statusBarShadow.animate().cancel();
            statusBarShadow.animate().alpha(0f).setDuration(250).withEndAction(() -> {
                statusBarShadow.setVisibility(View.GONE);
            }).start();
        }
    }

    public void updateStatusBarShadow() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment appsDrawer = fm.findFragmentByTag("apps");
        boolean hasAppsDrawer = (appsDrawer != null && !appsDrawer.isRemoving())
                || (homeScreenFragment != null && homeScreenFragment.isAdded() && homeScreenFragment.hasAppsDrawer());
        Fragment currentInContainer = fm.findFragmentById(R.id.container);
        boolean isHelpOrCredits = (currentInContainer instanceof HelpPage) || (currentInContainer instanceof CreditsPage);
        boolean show = hasAppsDrawer || isHelpOrCredits;
        setStatusBarShadowVisible(show);
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
                : (getResources().getDimensionPixelSize(R.dimen.notification_height_collapsed) + currentBottomInset);

        if (panelHeightAnimator != null) {
            panelHeightAnimator.cancel();
        }
        panelHeightAnimator = ValueAnimator.ofInt(notificationContainerFrame.getHeight(), targetHeight);
        panelHeightAnimator.addUpdateListener(valueAnimator -> {
            int value = (int) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = notificationContainerFrame.getLayoutParams();
            layoutParams.height = value;
            notificationContainerFrame.setLayoutParams(layoutParams);
        });
        panelHeightAnimator.setDuration(300);
        panelHeightAnimator.start();
    }

    private int computeExpandedTargetHeight() {
        Fragment notifFragment = getSupportFragmentManager().findFragmentByTag("notifications");
        int fallbackHeight = getResources().getDimensionPixelSize(R.dimen.notification_height_collapsed) + currentBottomInset;
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
        return measured + currentBottomInset;
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
                    .commit();
            if (fragment instanceof HomeScreenK) {
                homeScreenFragment = (HomeScreenK) fragment;
                updateBottomPadding();
            } else {
                homeScreenFragment = null;
            }
            updateStatusBarShadow();
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

        if (currentBottomInset == 0) {
            initWindowInsets();
        }

        ViewGroup.LayoutParams params = notificationContainerFrame.getLayoutParams();
        params.height = 0;
        notificationContainerFrame.setLayoutParams(params);
        notificationContainerFrame.setVisibility(View.VISIBLE);
        updateBottomPadding();

        int collapsedHeight = getResources().getDimensionPixelSize(R.dimen.notification_height_collapsed) + currentBottomInset;
        if (panelHeightAnimator != null) {
            panelHeightAnimator.cancel();
        }
        panelHeightAnimator = ValueAnimator.ofInt(0, collapsedHeight);
        panelHeightAnimator.addUpdateListener(va -> {
            ViewGroup.LayoutParams lp = notificationContainerFrame.getLayoutParams();
            lp.height = (int) va.getAnimatedValue();
            notificationContainerFrame.setLayoutParams(lp);
        });
        panelHeightAnimator.setDuration(300);
        panelHeightAnimator.start();
    }

    /** Slides the panel down out of view, then closes the gap. */
    private void hideNotificationPanel() {
        if (panelHeightAnimator != null) {
            panelHeightAnimator.cancel();
        }
        panelHeightAnimator = ValueAnimator.ofInt(notificationContainerFrame.getHeight(), 0);
        panelHeightAnimator.addUpdateListener(va -> {
            ViewGroup.LayoutParams lp = notificationContainerFrame.getLayoutParams();
            lp.height = (int) va.getAnimatedValue();
            notificationContainerFrame.setLayoutParams(lp);
        });
        panelHeightAnimator.setDuration(300);
        panelHeightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                notificationContainerFrame.setVisibility(View.GONE);
                isExpanded = false;
                updateBottomPadding();
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
        panelHeightAnimator.start();
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
        initWindowInsets();
        updateBottomPadding();
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
        updateStatusBarShadow();
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

    /**
     * Completely restarts the launcher activity from scratch, re-running onCreate with a clean task state.
     */
    public static void restart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
