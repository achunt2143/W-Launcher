package com.achunt.weboslauncher

import android.graphics.drawable.Icon
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NotificationRepository {
    private val notifications: MutableList<Notification> = mutableListOf()
    private val notificationIcons: MutableList<Icon> = mutableListOf()
    private val notificationLiveData: MutableLiveData<List<Notification>> = MutableLiveData()
    private val notificationIconLiveData: MutableLiveData<List<Icon>> = MutableLiveData()


    fun addNotification(notification: Notification) {
        println("received ${notification.title} ${notification.body} ${notification.date}")
        // Notify observers (e.g., your RecyclerView adapter) about the data change
        // You can use LiveData or any other mechanism for observing data changes
        if (notifications.any {
                it.title == notification.title &&
                        it.body == notification.body &&
                        it.date == notification.date
            }) {
            // Duplicate notification found, do not add
            return
        }
        if (notification.date == "0")
            return
        if (notification.body.isEmpty())
            return
        println("added ${notification.title}")
        // No duplicate found, add the notification
        notifications.add(notification)
        notificationIcons.add(notification.appIcon)
        notifyItemChanged(notification)
    }

    fun removeNotification(notification: Notification) {
        notifications.remove(notification)
        // Notify observers (e.g., your RecyclerView adapter) about the data change
        // You can use LiveData or any other mechanism for observing data changes
    }

    fun clearNotifications() {
        notifications.clear()
        // Notify observers (e.g., your RecyclerView adapter) about the data change
        // You can use LiveData or any other mechanism for observing data changes
    }

    fun notifyItemChanged(notification: Notification) {
        val index = notifications.indexOf(notification)
        if (index != -1) {
            notificationLiveData.value = notifications.toList()
            notificationIconLiveData.value = notificationIcons.toList()
        }
    }

    fun getNotificationById(notificationId: Int): Notification? {
        return notifications.find { it.id == notificationId }
    }

    fun getNotifications(): List<Notification> {
        println("notifications list has ${notifications.size} notifications to show")
        return notifications.toList()
    }

    fun getNotificationsIcons(): List<Icon> {
        notifications.iterator().forEach { notificationIcons.add(it.appIcon) }
        return notificationIcons.toList()
    }

    fun getNotificationLiveData(): LiveData<List<Notification>> {
        return notificationLiveData
    }

    fun getNotificationIconLiveData(): LiveData<List<Icon>> {
        return notificationIconLiveData
    }

    fun findNotificationByTitleAndBody(title: String, body: String): Notification? {
        return notifications.find { it.title == title && it.body == body }
    }

    fun findNotificationByTitle(title: String): Notification? {
        return notifications.find { it.title == title }
    }


    // You can add more functions here for managing notifications as needed
}
