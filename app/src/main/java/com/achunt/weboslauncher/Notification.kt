package com.achunt.weboslauncher

import android.app.PendingIntent
import android.graphics.drawable.Icon

data class NotificationAction(
    val title: CharSequence,
    val intent: PendingIntent?,
    val icon: Icon? = null
)

// Notification.kt
data class Notification(
    var appIcon: Icon,
    var title: String,
    var body: String,
    var date: String,
    val id: Int,
    val key: String,
    val packageName: String,
    var intent: PendingIntent?,
    var actions: List<NotificationAction> = emptyList(),
    val isAutoCancel: Boolean = true
)
