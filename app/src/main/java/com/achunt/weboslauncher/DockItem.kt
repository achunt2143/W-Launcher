package com.achunt.weboslauncher

/**
 * Lightweight model for a single dock slot.
 *
 * [packageName] is the only field persisted. Label and icon are resolved at
 * runtime from the PackageManager so we never store stale drawable data.
 */
data class DockItem(
    val packageName: String,
    val label: String,
    val isDrawerButton: Boolean = false
)
