package com.achunt.weboslauncher

import android.app.PendingIntent
import android.graphics.drawable.Icon

// Notification.kt
data class Notification(
    var appIcon: Icon,
    var title: String,
    var body: String,
    var date: String,
    val id: Int,
    val key: String,
    val packageName: String,
    val intent: PendingIntent?
)
