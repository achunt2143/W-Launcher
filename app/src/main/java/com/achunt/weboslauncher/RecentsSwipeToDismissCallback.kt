package com.achunt.weboslauncher

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Flick-up-to-close for the recents card carousel, mirroring webOS's "toss the card away"
 * gesture. Swipe direction is fixed to UP regardless of the carousel's horizontal scroll
 * axis — ItemTouchHelper distinguishes the two by drag angle, so a horizontal drag still
 * scrolls the RecyclerView as normal.
 */
class RecentsSwipeToDismissCallback(
    private val onDismiss: (position: Int) -> Unit
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean = false
    override fun isItemViewSwipeEnabled(): Boolean = true

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int = makeMovementFlags(0, ItemTouchHelper.UP)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onDismiss(viewHolder.adapterPosition)
    }
}
