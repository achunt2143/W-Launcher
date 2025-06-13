package com.achunt.weboslauncher

import android.content.Context
import android.graphics.drawable.Icon
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationItemIconAdapter(
    private val context: Context,
    private var notificationsIcons: List<Icon>,
    private val onIconClick: (Icon) -> Unit // Callback to expand full view
) : RecyclerView.Adapter<NotificationItemIconAdapter.NotificationIconViewHolder>() {

    inner class NotificationIconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIconSmall)
        val badgeCount: TextView = itemView.findViewById(R.id.badgeCount)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onIconClick(notificationsIcons[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationIconViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item_small, parent, false)
        return NotificationIconViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationIconViewHolder, position: Int) {
        val icon = notificationsIcons[position]
        holder.appIcon.setImageIcon(icon)

        // Count how many notifications have this exact icon
        val matchingCount = NotificationRepository.getNotifications().count {
            it.appIcon == icon
        }

        if (matchingCount > 1) {
            holder.badgeCount.text = matchingCount.toString()
            holder.badgeCount.visibility = View.VISIBLE
        } else {
            holder.badgeCount.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onIconClick(icon)
        }
    }

    override fun getItemCount(): Int = notificationsIcons.size

    fun updateData(newNotificationsIcons: List<Icon>) {
        notificationsIcons = newNotificationsIcons
        notifyDataSetChanged()
    }
}
