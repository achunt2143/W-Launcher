package com.achunt.weboslauncher

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils

/**
 * Single source of truth for the app's theme preferences:
 * - [MATERIAL_YOU]: Dynamic Monet system palette derived from the user's wallpaper (Android 12+)
 * - [LIGHT]: Authentic Palm webOS Classic Blue palette (#5B92B7 / #3E6C88)
 * - [DARK]: Authentic Palm webOS Obsidian Onyx glass (#4A4A4A / #202020)
 */
object ThemePreference {
    const val MATERIAL_YOU = "MaterialYou"
    const val LIGHT = "Light"
    const val DARK = "Dark"

    @JvmStatic
    fun isDark(themeName: String?): Boolean = themeName == DARK || themeName == "Classic3"

    @JvmStatic
    fun isMaterialYou(themeName: String?): Boolean = themeName == MATERIAL_YOU

    @JvmStatic
    fun isClassic(themeName: String?): Boolean = !isDark(themeName) && !isMaterialYou(themeName)

    @JvmStatic
    fun isDynamicColorAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    @JvmStatic
    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return prefs.getString("themeName", LIGHT) ?: LIGHT
    }

    /**
     * App drawer icon background squircle resource for legacy grids.
     */
    @JvmStatic
    fun iconBackgroundRes(context: Context): Int {
        val theme = getTheme(context)
        return if (isDark(theme)) {
            R.drawable.notif_icon_background
        } else {
            R.drawable.app_icon_bg_light
        }
    }

    /**
     * Legacy resource ID helper for dock background.
     */
    @JvmStatic
    fun dockBackgroundRes(context: Context): Int {
        val theme = getTheme(context)
        return if (isDark(theme)) {
            R.drawable.dock_glass_dark
        } else {
            R.drawable.dock_glass_light
        }
    }

    /**
     * Builds or loads the dock frosted-glass shelf drawable.
     * For [MATERIAL_YOU] on API 31+, generates a frosted glass shelf using the dynamic
     * Monet wallpaper accent tones with specular top highlight line.
     */
    @JvmStatic
    fun getDockBackground(context: Context): Drawable {
        val theme = getTheme(context)
        if (isDark(theme)) {
            return AppCompatResources.getDrawable(context, R.drawable.dock_glass_dark)!!
        }
        if (isMaterialYou(theme) && isDynamicColorAvailable()) {
            return buildMaterialYouDockDrawable(context)
        }
        return AppCompatResources.getDrawable(context, R.drawable.dock_glass_light)!!
    }

    private fun buildMaterialYouDockDrawable(context: Context): Drawable {
        val density = context.resources.displayMetrics.density
        val topColor = ColorUtils.setAlphaComponent(
            ContextCompat.getColor(context, android.R.color.system_accent1_200), 225
        )
        val bottomColor = ColorUtils.setAlphaComponent(
            ContextCompat.getColor(context, android.R.color.system_accent1_400), 245
        )
        val strokeColor = ColorUtils.setAlphaComponent(
            ContextCompat.getColor(context, android.R.color.system_accent1_100), 80
        )
        val cornerRadius = 20f * density

        val base = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(topColor, bottomColor)).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(
                cornerRadius, cornerRadius, // top-left
                cornerRadius, cornerRadius, // top-right
                0f, 0f,                     // bottom-right
                0f, 0f                      // bottom-left
            )
            setStroke((0.5f * density).toInt().coerceAtLeast(1), strokeColor)
        }

        val highlightColor = ColorUtils.setAlphaComponent(Color.WHITE, 60)
        val highlight = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(highlightColor)
            cornerRadii = floatArrayOf(
                cornerRadius, cornerRadius,
                cornerRadius, cornerRadius,
                0f, 0f,
                0f, 0f
            )
        }

        val layer = LayerDrawable(arrayOf(base, highlight))
        val insetBottom = (62.5f * density).toInt()
        layer.setLayerInset(1, 0, 0, 0, insetBottom)
        return layer
    }

    /**
     * Header bar background drawable (for App Drawer, Settings, Credits).
     */
    @JvmStatic
    fun getHeaderDrawable(context: Context): Drawable {
        val theme = getTheme(context)
        if (isDark(theme)) {
            return AppCompatResources.getDrawable(context, R.drawable.webos_header_bg)!!
        }
        if (isMaterialYou(theme) && isDynamicColorAvailable()) {
            val startColor = ContextCompat.getColor(context, android.R.color.system_accent1_300)
            val endColor = ContextCompat.getColor(context, android.R.color.system_accent1_600)
            return GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(startColor, endColor))
        }
        return AppCompatResources.getDrawable(context, R.drawable.webos_header_bg_light)!!
    }

    /**
     * Search pill background drawable for the App Drawer.
     */
    @JvmStatic
    fun getSearchPillDrawable(context: Context): Drawable {
        val theme = getTheme(context)
        if (isDark(theme)) {
            return AppCompatResources.getDrawable(context, R.drawable.search_pill_bg)!!
        }
        if (isMaterialYou(theme) && isDynamicColorAvailable()) {
            val density = context.resources.displayMetrics.density
            val pillFill = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, android.R.color.system_neutral1_100), 220
            )
            val pillStroke = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, android.R.color.system_neutral2_300), 80
            )
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f * density
                setColor(pillFill)
                setStroke((1f * density).toInt().coerceAtLeast(1), pillStroke)
            }
        }
        return AppCompatResources.getDrawable(context, R.drawable.search_pill_bg_light)!!
    }

    /**
     * Active tab pill indicator drawable for TabLayout with 16dp horizontal text padding.
     */
    @JvmStatic
    fun getTabIndicator(context: Context): Drawable {
        val theme = getTheme(context)
        if (isDark(theme)) {
            return AppCompatResources.getDrawable(context, R.drawable.tab_pill_indicator)!!
        }
        if (isMaterialYou(theme) && isDynamicColorAvailable()) {
            val density = context.resources.displayMetrics.density
            val pillFill = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, android.R.color.system_accent1_100), 90
            )
            val pillStroke = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, android.R.color.system_accent1_50), 120
            )
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * density
                setColor(pillFill)
                setStroke((1f * density).toInt().coerceAtLeast(1), pillStroke)
            }
            val insetH = -(16f * density).toInt()
            val insetV = (3f * density).toInt()
            return InsetDrawable(shape, insetH, insetV, insetH, insetV)
        }
        return AppCompatResources.getDrawable(context, R.drawable.tab_pill_indicator_light)!!
    }

    /**
     * App Drawer frosted glass backdrop tint color.
     */
    @JvmStatic
    fun getBackdropTintColor(context: Context): Int {
        val theme = getTheme(context)
        return when {
            isDark(theme) -> Color.TRANSPARENT
            isMaterialYou(theme) && isDynamicColorAvailable() -> {
                val color = ContextCompat.getColor(context, android.R.color.system_accent1_700)
                ColorUtils.setAlphaComponent(color, 185)
            }
            else -> {
                val color = ContextCompat.getColor(context, R.color.webos_accent_blue_dark)
                ColorUtils.setAlphaComponent(color, 190)
            }
        }
    }

    @JvmStatic
    fun getSearchTextColor(context: Context): Int {
        val theme = getTheme(context)
        return when {
            isDark(theme) -> ContextCompat.getColor(context, R.color.webos_search_text_dark)
            isMaterialYou(theme) && isDynamicColorAvailable() -> ContextCompat.getColor(context, android.R.color.system_neutral1_900)
            else -> ContextCompat.getColor(context, R.color.webos_search_text_light)
        }
    }

    @JvmStatic
    fun getSearchHintColor(context: Context): Int {
        val theme = getTheme(context)
        return when {
            isDark(theme) -> ContextCompat.getColor(context, R.color.webos_search_hint_dark)
            isMaterialYou(theme) && isDynamicColorAvailable() -> ContextCompat.getColor(context, android.R.color.system_neutral1_700)
            else -> ContextCompat.getColor(context, R.color.webos_search_hint_light)
        }
    }

    @JvmStatic
    fun getSearchIconColor(context: Context): Int {
        val theme = getTheme(context)
        return when {
            isDark(theme) -> ContextCompat.getColor(context, R.color.webos_search_icon_dark)
            isMaterialYou(theme) && isDynamicColorAvailable() -> ContextCompat.getColor(context, android.R.color.system_neutral1_700)
            else -> ContextCompat.getColor(context, R.color.webos_search_icon_light)
        }
    }
}
