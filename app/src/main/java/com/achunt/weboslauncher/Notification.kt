package com.achunt.weboslauncher

import android.graphics.drawable.Icon

// Notification.kt
data class Notification(
    val appIcon: Icon,
    var title: String,
    var body: String,
    var date: String,
    val id: Int
)
