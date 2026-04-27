package com.achunt.weboslauncher

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Drives the horizontal dock RecyclerView.
 *
 * Each item is a 56dp × 56dp icon. The last item is always the drawer-launcher
 * button (identified by [DockItem.isDrawerButton]).
 *
 * Interactions:
 *  - Tap → launch app  (or open drawer for drawer button)
 *  - Long-press → remove from dock  (no-op for drawer button)
 *
 * The adapter does NOT write to [DockRepository] itself — it delegates all
 * mutations back to the host fragment via [DockInteractionListener] so the
 * fragment can reload the full list and call [notifyDataSetChanged] cleanly.
 */
class DockAdapter(
    private val context: Context,
    private var items: MutableList<DockItem>,
    private val listener: DockInteractionListener
) : RecyclerView.Adapter<DockAdapter.DockViewHolder>() {

    interface DockInteractionListener {
        fun onDockAppClicked(item: DockItem)
        fun onDockDrawerClicked()
        fun onDockItemLongPressed(item: DockItem)
    }

    private val pm: PackageManager = context.packageManager

    inner class DockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.dock_item_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dock, parent, false)
        return DockViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: DockViewHolder, position: Int) {
        val item = items[position]

        if (item.isDrawerButton) {
            // Drawer launcher — always uses the static applauncher drawable
            val drawable = ResourcesCompat.getDrawable(
                context.resources, R.drawable.applauncher, context.theme
            )
            holder.icon.setImageDrawable(drawable)
            holder.icon.background = ResourcesCompat.getDrawable(
                context.resources, R.drawable.dockshadow, context.theme
            )
            holder.itemView.setOnClickListener { listener.onDockDrawerClicked() }
            holder.itemView.setOnLongClickListener(null)
        } else {
            holder.icon.setImageDrawable(loadIcon(item.packageName))
            holder.icon.background = ResourcesCompat.getDrawable(
                context.resources, R.drawable.dockshadow, context.theme
            )
            holder.itemView.setOnClickListener { listener.onDockAppClicked(item) }
            holder.itemView.setOnLongClickListener {
                listener.onDockItemLongPressed(item)
                true
            }
        }
    }

    fun updateItems(newItems: List<DockItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    private fun loadIcon(packageName: String): Drawable? {
        return try {
            pm.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            ResourcesCompat.getDrawable(context.resources, R.drawable.applauncher, context.theme)
        }
    }
}
