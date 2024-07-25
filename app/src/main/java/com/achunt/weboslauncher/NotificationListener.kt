package com.achunt.weboslauncher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Handle the posted notification
        val notificationId = sbn.id
        val packageName = sbn.notification.extras.getCharSequence("android.title")?.toString() ?: ""
        val notificationText =
            sbn.notification.extras.getCharSequence("android.text")?.toString() ?: ""
        val date = sbn.notification.`when`
        val icon = sbn.notification.smallIcon ?: sbn.notification.getLargeIcon()
        println("icon $icon")

        if (packageName.endsWith("new messages")) {
            // Ignore these notifications
            return
        }
        if (packageName.contentEquals("Missed calls")) {
            return
        }

        // Check if a notification with the same ID already exists
        val existingNotification = NotificationRepository.getNotificationById(notificationId)
        if (existingNotification != null) {
            // Notification with the same ID exists
            if (isSimilarNotification(existingNotification, packageName, notificationText)) {
                // Update the date of the existing notification
                existingNotification.date = date.toString()
                NotificationRepository.notifyItemChanged(existingNotification)
            } else {
                val sameNotificationTB = NotificationRepository.findNotificationByTitleAndBody(
                    packageName,
                    notificationText
                )
                val sameNotificationT = NotificationRepository.findNotificationByTitle(packageName)
                if (sameNotificationTB != null) {
                    // Update the date of the existing notification with the same title and body
                    sameNotificationTB.date = date.toString()
                    NotificationRepository.notifyItemChanged(sameNotificationTB)
                }
                if (sameNotificationT != null && date.toString() != "0" && notificationText != "Missed call") {
                    sameNotificationT.body = notificationText
                    sameNotificationT.date = date.toString()
                    NotificationRepository.notifyItemChanged(sameNotificationT)
                }
                if (sameNotificationT == null && sameNotificationTB == null) {
                    // If the new notification is not similar to any existing one, add it as a new notification
                    val notification = Notification(
                        icon,
                        packageName,
                        notificationText,
                        date.toString(),
                        notificationId
                    )
                    NotificationRepository.addNotification(notification)
                }
                if (!isSimilarNotification(existingNotification, packageName, notificationText)) {
                    val notification = Notification(
                        icon,
                        packageName,
                        notificationText,
                        date.toString(),
                        notificationId
                    )
                    NotificationRepository.addNotification(notification)
                }
            }
        } else {
            // Notification with this ID doesn't exist, add it to the list
            val notification =
                Notification(icon, packageName, notificationText, date.toString(), notificationId)
            NotificationRepository.addNotification(notification)
        }
    }

    private fun isSimilarNotification(
        existingNotification: Notification,
        newTitle: String,
        newText: String
    ): Boolean {
        // Compare the title and text of the new notification with the existing one
        // You can define your own logic here to determine similarity
        // For example, you can check if the titles are equal and ignore numbers in the title
        // or check if the texts are similar based on some criteria
        return existingNotification.title == newTitle && existingNotification.body == newText
    }

    private fun isSimilarNotification(
        existingNotification: Notification,
        newTitle: String,
        newID: Int
    ): Boolean {
        // Compare the title and text of the new notification with the existing one
        // You can define your own logic here to determine similarity
        // For example, you can check if the titles are equal and ignore numbers in the title
        // or check if the texts are similar based on some criteria
        return existingNotification.title == newTitle && existingNotification.id == newID
    }
}

