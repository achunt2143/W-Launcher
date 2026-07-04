package com.achunt.weboslauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Collections;
import java.util.List;

/**
 * The long-press "app actions" popup Android launchers show on an icon: the app's own
 * dynamic/manifest shortcuts (e.g. Gmail's "Compose"), plus standard actions that vary
 * by where the icon lives (see [Source]). Shortcuts only appear once this app is actually
 * set as the default launcher (LauncherApps.getShortcuts requires shortcut-host permission)
 * — until then this quietly falls back to just the standard actions.
 */
public final class AppActionsMenu {

    private static final String TAG = "AppActionsMenu";

    /** Where the long-pressed icon lives, since that changes which actions make sense:
     * a drawer icon isn't in the dock yet (offer Add to Dock / Uninstall), while a dock
     * icon already is (offer Remove from Dock instead — no point offering both, and
     * uninstalling from the dock's context menu isn't standard launcher behavior). */
    public enum Source { DRAWER, DOCK }

    private AppActionsMenu() {
    }

    /** For apps in the current user profile (System/Downloads tabs, and the dock). */
    public static void show(View anchor, String packageName, Source source) {
        show(anchor, packageName, Process.myUserHandle(), source);
    }

    /** For apps in another profile (the Work tab) — skips actions that don't make sense
     * cross-profile (adding to the dock, which can't launch another profile's apps; and
     * uninstalling, which needs device-admin APIs this app doesn't have). */
    public static void show(View anchor, String packageName, UserHandle userHandle, Source source) {
        Context context = anchor.getContext();
        boolean isCurrentUser = userHandle.equals(Process.myUserHandle());

        PopupMenu popup = new PopupMenu(context, anchor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popup.setForceShowIcon(true);
        }
        Menu menu = popup.getMenu();
        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);

        for (ShortcutInfo shortcut : loadShortcuts(launcherApps, packageName, userHandle)) {
            MenuItem item = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, shortcut.getShortLabel());
            if (launcherApps != null) {
                Drawable icon = launcherApps.getShortcutIconDrawable(shortcut, 0);
                if (icon != null) item.setIcon(icon);
            }
            item.setOnMenuItemClickListener(mi -> {
                launchShortcut(launcherApps, shortcut);
                return true;
            });
        }

        if (source == Source.DOCK) {
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.app_action_remove_from_dock)
                    .setOnMenuItemClickListener(mi -> {
                        removeFromDock(context, packageName);
                        return true;
                    });
        } else if (isCurrentUser) {
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.app_action_add_to_dock)
                    .setOnMenuItemClickListener(mi -> {
                        addToDock(context, packageName);
                        return true;
                    });
        }

        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.app_action_app_info)
                .setOnMenuItemClickListener(mi -> {
                    openAppInfo(context, launcherApps, packageName, userHandle, isCurrentUser);
                    return true;
                });

        if (source == Source.DRAWER && isCurrentUser && !isSystemApp(context, packageName)) {
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.app_action_uninstall)
                    .setOnMenuItemClickListener(mi -> {
                        uninstall(context, packageName);
                        return true;
                    });
        }

        popup.show();
    }

    private static List<ShortcutInfo> loadShortcuts(LauncherApps launcherApps, String packageName, UserHandle userHandle) {
        if (launcherApps == null || !launcherApps.hasShortcutHostPermission()) return Collections.emptyList();
        LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
        query.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);
        query.setPackage(packageName);
        try {
            List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, userHandle);
            return shortcuts != null ? shortcuts : Collections.emptyList();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load shortcuts for " + packageName, e);
            return Collections.emptyList();
        }
    }

    private static void launchShortcut(LauncherApps launcherApps, ShortcutInfo shortcut) {
        if (launcherApps == null) return;
        try {
            launcherApps.startShortcut(shortcut, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch shortcut " + shortcut.getId(), e);
        }
    }

    private static void addToDock(Context context, String packageName) {
        HomeScreenK home = findHomeScreen(context);
        if (home != null) home.addToDock(packageName);
    }

    private static void removeFromDock(Context context, String packageName) {
        HomeScreenK home = findHomeScreen(context);
        if (home != null) home.removeFromDock(packageName);
    }

    private static HomeScreenK findHomeScreen(Context context) {
        if (!(context instanceof FragmentActivity)) return null;
        Fragment home = ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("home");
        return home instanceof HomeScreenK ? (HomeScreenK) home : null;
    }

    private static void openAppInfo(Context context, LauncherApps launcherApps, String packageName,
                                     UserHandle userHandle, boolean isCurrentUser) {
        if (isCurrentUser) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", packageName, null));
            context.startActivity(intent);
            return;
        }
        if (launcherApps == null) return;
        List<LauncherActivityInfo> activities = launcherApps.getActivityList(packageName, userHandle);
        if (!activities.isEmpty()) {
            ComponentName component = activities.get(0).getComponentName();
            launcherApps.startAppDetailsActivity(component, userHandle, null, null);
        }
    }

    private static void uninstall(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.fromParts("package", packageName, null));
        context.startActivity(intent);
    }

    private static boolean isSystemApp(Context context, String packageName) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return (info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
