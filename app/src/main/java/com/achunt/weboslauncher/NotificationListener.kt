package com.achunt.weboslauncher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.text.format.DateUtils
import android.util.Log

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"

        // Set once the binder to the system's NotificationManager is actually live
        // (onListenerConnected), so requestCancel() never fires before it's safe to.
        private var instance: NotificationListener? = null

        /** Cancels the system notification identified by [key]. No-op if not currently connected. */
        fun requestCancel(key: String) {
            val listener = instance
            if (listener == null) {
                Log.w(TAG, "Cannot cancel $key — listener not connected")
                return
            }
            try {
                listener.cancelNotification(key)
            } catch (e: SecurityException) {
                Log.w(TAG, "Cancel denied for $key: ${e.message}")
            }
        }
    }

    /**
     * Fires whenever the service (re)binds — including after the launcher process was
     * killed and restarted, which onNotificationPosted alone never catches since Android
     * does not replay old postings. Reconciling against getActiveNotifications() here is
     * what makes the notification list survive a process death instead of coming back empty.
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this

        val active = try {
            activeNotifications
        } catch (e: SecurityException) {
            Log.w(TAG, "Unable to read active notifications on connect: ${e.message}")
            null
        } ?: return

        Log.d(TAG, "Listener connected — syncing with ${active.size} active notification(s)")
        val activeKeys = active.map { it.key }.toSet()
        NotificationRepository.getNotifications()
            .filter { it.key !in activeKeys }
            .forEach { NotificationRepository.removeNotification(it) }
        active.forEach { processStatusBarNotification(it) }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) {
            Log.w(TAG, "Null notification received. Skipping.")
            return
        }
        processStatusBarNotification(sbn)
    }

    private fun processStatusBarNotification(sbn: StatusBarNotification) {
        val id = sbn.id
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val postTime = if (sbn.notification.`when` > 0L) sbn.notification.`when` else System.currentTimeMillis()
        val icon: Icon? = sbn.notification.getLargeIcon() ?: sbn.notification.smallIcon
        val intent: PendingIntent? = sbn.notification.contentIntent
        val friendlyTime = DateUtils.getRelativeTimeSpanString(
            postTime,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()


        if (icon == null || title.isEmpty() || text.isEmpty()) {
            Log.d(TAG, "Notification missing essential data. Skipping. [id=$id, title=$title]")
            return
        }

        // Optional: Filter unwanted messages
        if (title.equals("Missed calls", true) || title.endsWith("new messages", true) || text.equals("Missed call", true)) {
            Log.d(TAG, "Filtered out system notification: $title / $text")
            return
        }

        val existing = NotificationRepository.getNotificationByKey(sbn.key)

        if (existing != null) {
            if (existing.title == title && existing.body == text) {
                existing.date = friendlyTime
                NotificationRepository.notifyItemChanged(existing)
                Log.d(TAG, "Updated timestamp of existing notification: $title")
            } else {
                existing.title = title
                existing.body = text
                existing.date = friendlyTime
                existing.appIcon = icon
                NotificationRepository.notifyItemChanged(existing)
                Log.d(TAG, "Updated content of existing notification: $title")
            }
        } else {
            val newNotification = Notification(
                id = id,
                key = sbn.key,
                packageName = sbn.packageName,
                title = title,
                body = text,
                appIcon = icon,
                date = friendlyTime,
                intent = intent,
            )
            NotificationRepository.addNotification(newNotification)
            Log.d(TAG, "Added new notification: $title")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val notification = NotificationRepository.getNotificationByKey(sbn.key)
        if (notification != null) {
            NotificationRepository.removeNotification(notification)
            Log.d(TAG, "Notification removed: ${notification.title}")
        }
    }
}
