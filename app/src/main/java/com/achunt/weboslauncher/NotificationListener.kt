package com.achunt.weboslauncher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.graphics.drawable.Icon
import android.util.Log

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) {
            Log.w(TAG, "Null notification received. Skipping.")
            return
        }

        val id = sbn.id
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val postTime = if (sbn.notification.`when` > 0L) sbn.notification.`when` else System.currentTimeMillis()
        val icon: Icon? = sbn.notification.getLargeIcon() ?: sbn.notification.smallIcon

        if (icon == null || title.isEmpty() || text.isEmpty()) {
            Log.d(TAG, "Notification missing essential data. Skipping. [id=$id, title=$title]")
            return
        }

        // Optional: Filter unwanted messages
        if (title.equals("Missed calls", true) || title.endsWith("new messages", true) || text.equals("Missed call", true)) {
            Log.d(TAG, "Filtered out system notification: $title / $text")
            return
        }

        val existing = NotificationRepository.getNotificationById(id)

        if (existing != null) {
            if (existing.title == title && existing.body == text) {
                existing.date = postTime.toString()
                NotificationRepository.notifyItemChanged(existing)
                Log.d(TAG, "Updated timestamp of existing notification: $title")
            } else {
                existing.title = title
                existing.body = text
                existing.date = postTime.toString()
                existing.appIcon = icon
                NotificationRepository.notifyItemChanged(existing)
                Log.d(TAG, "Updated content of existing notification: $title")
            }
        } else {
            val newNotification = com.achunt.weboslauncher.Notification(
                id = id,
                title = title,
                body = text,
                appIcon = icon,
                date = postTime.toString()
            )
            NotificationRepository.addNotification(newNotification)
            Log.d(TAG, "Added new notification: $title")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val id = sbn.id
        val notification = NotificationRepository.getNotificationById(id)
        if (notification != null) {
            NotificationRepository.removeNotification(notification)
            Log.d(TAG, "Notification removed: ${notification.title}")
        }
    }
}
