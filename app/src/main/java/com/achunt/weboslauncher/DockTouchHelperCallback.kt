package com.achunt.weboslauncher

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Drives drag-to-reorder for the dock RecyclerView.
 *
 * Drag is started manually (via [DockAdapter.DockInteractionListener.onDockDragRequested])
 * rather than through [isLongPressDragEnabled], so a long-press that never moves still
 * falls through to [onLongPressWithoutMove] — preserving the existing "long-press to show
 * app actions" popup for a plain long-press, while a long-press-and-drag reorders instead.
 *
 * The drawer-launcher button (always the last item) can never be dragged, and nothing
 * can be dropped past it — enforced via [getMovementFlags] and [onMove].
 */
class DockTouchHelperCallback(
    private val adapter: DockAdapter,
    private val onReorderFinished: (fromPosition: Int, toPosition: Int) -> Unit,
    private val onLongPressWithoutMove: (position: Int, itemView: View) -> Unit
) : ItemTouchHelper.Callback() {

    private var dragStartPosition = -1
    private var moved = false

    override fun isLongPressDragEnabled(): Boolean = false
    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val item = adapter.getItem(viewHolder.adapterPosition)
        if (item == null || item.isDrawerButton) return 0
        return makeMovementFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition
        val targetItem = adapter.getItem(to) ?: return false
        if (targetItem.isDrawerButton) return false
        moved = true
        adapter.moveItem(from, to)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Swiping is disabled; nothing to do.
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
            dragStartPosition = viewHolder.adapterPosition
            moved = false
            viewHolder.itemView.alpha = 0.7f
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = 1.0f

        if (dragStartPosition == -1) return
        if (moved) {
            onReorderFinished(dragStartPosition, viewHolder.adapterPosition)
        } else {
            onLongPressWithoutMove(dragStartPosition, viewHolder.itemView)
        }
        dragStartPosition = -1
        moved = false
    }
}
