package com.achunt.weboslauncher

import android.content.Context
import android.graphics.drawable.Icon
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class NotificationItemIconAdapter(
    private val context: Context,
    private var notificationsIcons: List<Icon>
) :
    RecyclerView.Adapter<NotificationItemIconAdapter.NotificationIconViewHolder>() {

    inner class NotificationIconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIconSmall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationIconViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item_small, parent, false)
        return NotificationIconViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationIconViewHolder, position: Int) {
        val notificationIcon = notificationsIcons[position]
        // Set the app icon
        holder.appIcon.setImageIcon(notificationIcon)

    }

    override fun getItemCount(): Int {
        return notificationsIcons.size
    }

    fun updateData(newNotificationsIcons: List<Icon>) {
        notificationsIcons = newNotificationsIcons
        notifyDataSetChanged()
    }
}
