package com.achunt.weboslauncher

import android.graphics.drawable.Icon
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.CopyOnWriteArrayList

object NotificationRepository {
    private val notifications: MutableList<Notification> = CopyOnWriteArrayList()
    private val notificationIcons: MutableList<Icon> = CopyOnWriteArrayList()
    private val notificationLiveData: MutableLiveData<List<Notification>> = MutableLiveData(
        emptyList()
    )
    private val notificationIconLiveData: MutableLiveData<List<Icon>> = MutableLiveData(emptyList())
    private val lock = Any()


    fun addNotification(notification: Notification) {
        synchronized(lock) {
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
        }
        notifyItemChanged(notification)
    }

    fun removeNotification(notification: Notification) {
        synchronized(lock) {
            val removed = notifications.remove(notification)
            // if (removed) { // Only notify if something actually changed
            //     // No need to manage notificationIcons separately
            // }
            if (removed) notifyObserversOfChange()
        }

    }

    fun clearNotifications() {
        synchronized(lock) {
            if (notifications.isNotEmpty()) {
                notifications.clear()
                // No need to manage notificationIcons separately
            } else {
                return // No change, no need to notify
            }
        }
        notifyObserversOfChange()
    }

    private fun notifyObserversOfChange() {
        synchronized(lock) {
            notificationLiveData.postValue(notifications.toList())
            notificationIconLiveData.postValue(notifications.map { it.appIcon }.distinct())
        }
    }


    fun notifyItemChanged(notification: Notification) {
        synchronized(lock) {
            val index = notifications.indexOf(notification)
            if (index != -1) {
                notificationLiveData.value = notifications.toList()
                notificationIconLiveData.value = notifications.map { it.appIcon }.distinct()

            }
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
        synchronized(lock) {
            val uniqueIcons = notifications.map { it.appIcon }.distinct()
            notificationIcons.clear()
            notificationIcons.addAll(uniqueIcons)
            return notificationIcons.toList()
        }
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

    fun removeNotificationById(notificationId: Int) {
        synchronized(lock) {
            val notification = getNotificationById(notificationId)
            if (notification != null && notifications.remove(notification)) {
                notifyObserversOfChange()
            }
        }
    }



    // You can add more functions here for managing notifications as needed
}
