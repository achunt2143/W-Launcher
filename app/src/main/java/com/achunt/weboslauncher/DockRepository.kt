package com.achunt.weboslauncher

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONException

/**
 * Single source of truth for the dock item list.
 *
 * Persists an ordered JSON array of package-name strings in SharedPreferences
 * under the key [PREFS_NAME]/[KEY_DOCK]. The special drawer-button entry is
 * represented by the sentinel [DRAWER_SENTINEL] and is always appended last
 * (never stored — it is injected at read time).
 *
 * Default seed: phone → contacts → messages → browser, resolved via the
 * same SpecialApps prefs that HomeScreenK already uses, so existing users
 * keep their current dock on first launch.
 */
class DockRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val pm: PackageManager = context.packageManager

    companion object {
        const val PREFS_NAME = "DockPrefs"
        const val KEY_DOCK = "dock_items"
        const val DRAWER_SENTINEL = "__drawer__"
        const val MAX_DOCK_ITEMS = 6   // excludes drawer button
        private const val TAG = "DockRepository"
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the current dock list, with the drawer button appended. */
    fun getDockItems(): List<DockItem> {
        val packages = loadPackages()
        val items = packages.mapNotNull { pkg -> resolveDockItem(pkg) }.toMutableList()
        // Always append the drawer launcher button last
        items.add(DockItem(DRAWER_SENTINEL, "", isDrawerButton = true))
        return items
    }

    /** Adds [packageName] to the dock. No-op if already present or dock is full. */
    fun addItem(packageName: String): Boolean {
        val current = loadPackages()
        if (current.contains(packageName)) return false
        if (current.size >= MAX_DOCK_ITEMS) return false
        savePackages(current + packageName)
        return true
    }

    /** Removes [packageName] from the dock. */
    fun removeItem(packageName: String) {
        savePackages(loadPackages().filter { it != packageName })
    }

    /** Moves item at [fromIndex] to [toIndex] (both exclude the drawer button). */
    fun moveItem(fromIndex: Int, toIndex: Int) {
        val list = loadPackages().toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        savePackages(list)
    }

    /** True if the dock contains no persisted items yet (first launch). */
    fun isEmpty(): Boolean = !prefs.contains(KEY_DOCK)

    /**
     * Seeds the dock from the legacy SpecialApps SharedPreferences so that
     * existing users don't lose their configured phone/contacts/messages/browser.
     * Falls back to intent-resolution for any slot that has no saved package.
     */
    fun seedFromLegacy() {
        val special = context.getSharedPreferences("SpecialApps", Context.MODE_PRIVATE)
        val candidates = listOfNotNull(
            special.getString("PhonePackageName", null)
                ?: resolveDefaultPhone(),
            special.getString("ContactsPackageName", null)
                ?: resolveDefaultContacts(),
            special.getString("MessagesPackageName", null)
                ?: resolveDefaultMessages(),
            special.getString("BrowserPackageName", null)
                ?: resolveDefaultBrowser()
        ).distinct().filter { isInstalled(it) }

        savePackages(candidates)
        Log.d(TAG, "Seeded dock from legacy: $candidates")
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun loadPackages(): List<String> {
        val json = prefs.getString(KEY_DOCK, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse dock JSON", e)
            emptyList()
        }
    }

    private fun savePackages(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString(KEY_DOCK, arr.toString()).apply()
    }

    private fun resolveDockItem(packageName: String): DockItem? {
        return try {
            val info = pm.getApplicationInfo(packageName, 0)
            DockItem(
                packageName = packageName,
                label = pm.getApplicationLabel(info).toString()
            )
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Dock package not found, removing: $packageName")
            removeItem(packageName)   // auto-clean uninstalled apps
            null
        }
    }

    private fun isInstalled(pkg: String): Boolean = try {
        pm.getApplicationInfo(pkg, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) { false }

    private fun resolveDefaultPhone(): String? {
        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
        return pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }

    private fun resolveDefaultContacts(): String? {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        intent.data = android.net.Uri.parse("content://contacts")
        return pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }

    private fun resolveDefaultMessages(): String? {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO,
            android.net.Uri.parse("smsto:"))
        return pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }

    private fun resolveDefaultBrowser(): String? {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("http://"))
        return pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }
}
