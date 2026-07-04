package com.achunt.weboslauncher

import android.graphics.drawable.Icon

/** One app's stack of notifications — mirrors how webOS grouped cards per app. */
data class NotificationGroup(
    val packageName: String,
    val appIcon: Icon,
    val notifications: List<Notification>
)

/** Groups a flat notification list by app, preserving first-seen order for stable card positions. */
fun List<Notification>.grouped(): List<NotificationGroup> {
    val order = LinkedHashMap<String, MutableList<Notification>>()
    for (notification in this) {
        order.getOrPut(notification.packageName) { mutableListOf() }.add(notification)
    }
    return order.map { (packageName, notifications) ->
        NotificationGroup(packageName, notifications.last().appIcon, notifications)
    }
}
