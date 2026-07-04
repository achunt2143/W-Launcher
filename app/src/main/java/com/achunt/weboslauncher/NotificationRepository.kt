package com.achunt.weboslauncher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.CopyOnWriteArrayList

object NotificationRepository {
    private val notifications: MutableList<Notification> = CopyOnWriteArrayList()
    private val notificationLiveData: MutableLiveData<List<Notification>> = MutableLiveData(
        emptyList()
    )
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
        }
        notifyItemChanged(notification)
    }

    fun removeNotification(notification: Notification) {
        synchronized(lock) {
            val removed = notifications.remove(notification)
            if (removed) notifyObserversOfChange()
        }

    }

    fun clearNotifications() {
        synchronized(lock) {
            if (notifications.isNotEmpty()) {
                notifications.clear()
            } else {
                return // No change, no need to notify
            }
        }
        notifyObserversOfChange()
    }

    private fun notifyObserversOfChange() {
        synchronized(lock) {
            notificationLiveData.postValue(notifications.toList())
        }
    }


    fun notifyItemChanged(notification: Notification) {
        synchronized(lock) {
            val index = notifications.indexOf(notification)
            if (index != -1) {
                notificationLiveData.value = notifications.toList()
            }
        }
    }

    fun getNotificationById(notificationId: Int): Notification? {
        return notifications.find { it.id == notificationId }
    }

    /** [Notification.key] (StatusBarNotification.key) is the true unique identity — id alone can collide across apps. */
    fun getNotificationByKey(key: String): Notification? {
        return notifications.find { it.key == key }
    }

    /** Dismisses a single notification: cancels it on the system side and removes it locally. */
    fun dismiss(notification: Notification) {
        NotificationListener.requestCancel(notification.key)
        removeNotification(notification)
    }

    /** Dismisses every notification in [group] (e.g. a "clear all" action for one app). */
    fun dismissAll(group: List<Notification>) {
        group.forEach { dismiss(it) }
    }

    fun getNotifications(): List<Notification> {
        println("notifications list has ${notifications.size} notifications to show")
        return notifications.toList()
    }

    fun getNotificationLiveData(): LiveData<List<Notification>> {
        return notificationLiveData
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
