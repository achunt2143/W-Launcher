package com.achunt.weboslauncher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationItemIconAdapter(
    private val context: Context,
    private var groups: List<NotificationGroup>,
    private val onGroupClick: (NotificationGroup) -> Unit // Callback to expand full view
) : RecyclerView.Adapter<NotificationItemIconAdapter.NotificationIconViewHolder>() {

    inner class NotificationIconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIconSmall)
        val badgeCount: TextView = itemView.findViewById(R.id.badgeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationIconViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item_small, parent, false)
        return NotificationIconViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationIconViewHolder, position: Int) {
        val group = groups[position]
        holder.appIcon.setImageIcon(group.appIcon)

        val count = group.notifications.size
        if (count > 1) {
            holder.badgeCount.text = count.toString()
            holder.badgeCount.visibility = View.VISIBLE
        } else {
            holder.badgeCount.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onGroupClick(group) }
    }

    override fun getItemCount(): Int = groups.size

    fun updateData(newGroups: List<NotificationGroup>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
