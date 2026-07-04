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
 *
 * Drag-to-reorder is driven externally by a [DockTouchHelperCallback] attached
 * to the RecyclerView; [moveItem] only updates the in-memory list and animates
 * the move, it does not persist — the fragment persists once the drag ends.
 */
class DockAdapter(
    private val context: Context,
    private var items: MutableList<DockItem>,
    private val listener: DockInteractionListener
) : RecyclerView.Adapter<DockAdapter.DockViewHolder>() {

    interface DockInteractionListener {
        fun onDockAppClicked(item: DockItem)
        fun onDockDrawerClicked()
        fun onDockDragRequested(viewHolder: RecyclerView.ViewHolder)
    }

    private val pm: PackageManager = context.packageManager
    private var recyclerViewWidth = 0

    inner class DockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.dock_item_icon)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        // holder.itemView.parent isn't set yet at bind time (the LayoutManager attaches it
        // only after binding), so we can't read the RecyclerView's width off the item itself —
        // grab it directly here instead, and again whenever it changes size.
        recyclerViewWidth = recyclerView.width
        recyclerView.addOnLayoutChangeListener { v, left, _, right, _, _, _, _, _ ->
            val newWidth = right - left
            if (newWidth > 0 && newWidth != recyclerViewWidth) {
                recyclerViewWidth = newWidth
                // Defer — we're inside the RecyclerView's own layout pass here, and
                // notifyDataSetChanged() isn't allowed to run synchronously during one.
                v.post { notifyDataSetChanged() }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dock, parent, false)
        return DockViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: DockViewHolder, position: Int) {
        val item = items[position]

        // Divide the dock's full width evenly across however many items it currently holds,
        // rather than letting each item wrap to its own icon size — otherwise a dock with
        // fewer than a full row of icons clumps to the start instead of spanning the screen.
        if (recyclerViewWidth > 0 && itemCount > 0) {
            val slotWidth = recyclerViewWidth / itemCount
            val lp = holder.itemView.layoutParams
            if (lp.width != slotWidth) {
                lp.width = slotWidth
                holder.itemView.layoutParams = lp
            }
        }

        holder.icon.setBackgroundResource(ThemePreference.iconBackgroundRes(context))
        if (item.isDrawerButton) {
            // Drawer launcher — always uses the static applauncher drawable
            val drawable = ResourcesCompat.getDrawable(
                context.resources, R.drawable.applauncher, context.theme
            )
            holder.icon.setImageDrawable(drawable)
            holder.itemView.setOnClickListener { listener.onDockDrawerClicked() }
            holder.itemView.setOnLongClickListener(null)
        } else {
            holder.icon.setImageDrawable(loadIcon(item.packageName))
            holder.itemView.setOnClickListener { listener.onDockAppClicked(item) }
            holder.itemView.setOnLongClickListener {
                listener.onDockDragRequested(holder)
                true
            }
        }
    }

    fun updateItems(newItems: List<DockItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): DockItem? = items.getOrNull(position)

    /** Moves the item at [fromPosition] to [toPosition] in-memory and animates it. Does not persist. */
    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun loadIcon(packageName: String): Drawable? {
        return try {
            pm.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            ResourcesCompat.getDrawable(context.resources, R.drawable.applauncher, context.theme)
        }
    }
}
