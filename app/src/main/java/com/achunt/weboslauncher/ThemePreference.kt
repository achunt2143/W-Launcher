package com.achunt.weboslauncher

import android.content.Context

/**
 * Single source of truth for the app's Light/Dark theme preference, replacing the old
 * five-way Classic/Classic3/Mochi/Modern/System picker. Light mirrors the old "Classic"
 * look, Dark mirrors "Classic3".
 */
object ThemePreference {
    const val LIGHT = "Light"
    const val DARK = "Dark"

    /**
     * True if [themeName] should render as Dark. Old installs may still have "Classic3"
     * saved from before this rename — treated as Dark so those users don't silently revert
     * to Light. Anything else (null, "Classic", or a since-removed name like "Mochi") falls
     * back to Light.
     */
    @JvmStatic
    fun isDark(themeName: String?): Boolean = themeName == DARK || themeName == "Classic3"

    /**
     * The app-drawer icon squircle for the current theme — Dark reuses the same gunmetal
     * badge as notification icons; Light gets its own frosted-blue tile instead of that same
     * dark badge sitting oddly on the blue wallpaper. Reads SharedPreferences fresh each call
     * rather than caching, since the RAdapter* instances that call this are themselves cached
     * across apps-drawer sessions and must reflect a theme change immediately.
     */
    @JvmStatic
    fun iconBackgroundRes(context: Context): Int {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return if (isDark(prefs.getString("themeName", LIGHT))) {
            R.drawable.notif_icon_background
        } else {
            R.drawable.app_icon_bg_light
        }
    }

    /**
     * The dock's frosted-glass tray background for the current theme — same blue/gunmetal
     * gradient family as the matching header, just translucent so it reads as glass over
     * whatever's behind it (wallpaper, or the apps-drawer backdrop) instead of a flat bar.
     */
    @JvmStatic
    fun dockBackgroundRes(context: Context): Int {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return if (isDark(prefs.getString("themeName", LIGHT))) {
            R.drawable.dock_glass_dark
        } else {
            R.drawable.dock_glass_light
        }
    }
}
