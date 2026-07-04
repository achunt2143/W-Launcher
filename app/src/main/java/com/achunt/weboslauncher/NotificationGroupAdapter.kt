package com.achunt.weboslauncher

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

private const val TAG = "NotificationGroupAdapter"
private const val TOUCH_SLOP_PX = 12
private const val PEEK_SCALE = 0.94f

/**
 * One row per app: a real card STACK (not a horizontally scrolling list) of that app's
 * notifications, mirroring Palm webOS's dashboard cards. Only the front notification is a
 * live, touchable view; the notification behind it is a second real (not decorative) card,
 * scaled down and shifted down slightly, that slides/grows into full size in sync with the
 * front card's own drag — so the stack always shows actual content and reacts continuously
 * to touch. All of the transitions here are slides (translation) and scale, never fades —
 * only position and size change. Gestures on the front card:
 *  - swipe left/right → dismiss the front notification (system + local); the next one takes its place
 *  - swipe up          → cycle to the next notification in the stack, wrapping back to the first
 *                        once the last one is reached, so the whole stack is always reachable
 *  - swipe down         → reserved for collapsing the whole notification panel (delegates to [onSwipeDownToCollapse])
 *  - tap                → launch the notification's content intent
 */
class NotificationGroupAdapter(
    private val context: Context,
    private var groups: List<NotificationGroup>,
    private val onSwipeDownToCollapse: () -> Unit
) : RecyclerView.Adapter<NotificationGroupAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val frontCard: View = itemView.findViewById(R.id.frontCard)
        private val frontIcon: ImageView = frontCard.findViewById(R.id.appIcon)
        private val frontBadge: TextView = frontCard.findViewById(R.id.badgeCount)
        private val frontTitle: TextView = frontCard.findViewById(R.id.title)
        private val frontBody: TextView = frontCard.findViewById(R.id.body)
        private val frontDate: TextView = frontCard.findViewById(R.id.date)

        private val nextCard: View = itemView.findViewById(R.id.nextCard)
        private val nextIcon: ImageView = nextCard.findViewById(R.id.appIcon)
        private val nextTitle: TextView = nextCard.findViewById(R.id.title)
        private val nextBody: TextView = nextCard.findViewById(R.id.body)
        private val nextDate: TextView = nextCard.findViewById(R.id.date)

        private var notifications: List<Notification> = emptyList()
        private var currentIndex = 0

        private var downX = 0f
        private var downY = 0f
        private var dragging = false

        init {
            frontCard.setOnTouchListener { _, event -> handleTouch(event) }
        }

        fun bind(group: NotificationGroup) {
            notifications = group.notifications
            currentIndex = 0
            frontCard.translationX = 0f
            frontCard.translationY = 0f
            frontCard.scaleX = 1f
            frontCard.scaleY = 1f
            renderFront()
            renderNext()
        }

        private fun renderFront() {
            val notification = notifications.getOrNull(currentIndex) ?: return
            frontTitle.text = notification.title
            frontBody.text = notification.body
            frontDate.text = notification.date
            try {
                frontIcon.setImageIcon(notification.appIcon)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set app icon: ${e.message}")
            }

            val remaining = notifications.size - currentIndex
            if (remaining > 1) {
                frontBadge.text = remaining.toString()
                frontBadge.visibility = View.VISIBLE
            } else {
                frontBadge.visibility = View.GONE
            }
        }

        /** Binds the peeking card behind the front one to whatever comes next, reset to its resting (scaled-down, dimmed) state. */
        private fun renderNext() {
            if (notifications.size <= 1) {
                nextCard.visibility = View.GONE
                return
            }
            nextCard.visibility = View.VISIBLE
            val nextIndex = if (currentIndex >= notifications.lastIndex) 0 else currentIndex + 1
            val notification = notifications[nextIndex]
            nextTitle.text = notification.title
            nextBody.text = notification.body
            nextDate.text = notification.date
            try {
                nextIcon.setImageIcon(notification.appIcon)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set next app icon: ${e.message}")
            }
            resetNextCardTransform()
        }

        private fun resetNextCardTransform() {
            nextCard.translationX = 0f
            nextCard.translationY = peekOffsetPx()
            nextCard.scaleX = PEEK_SCALE
            nextCard.scaleY = PEEK_SCALE
        }

        /** Shifts the resting peek card down so its title/body sit below the front card's own
         * text instead of showing through it — only a sliver of card background peeks out. */
        private fun peekOffsetPx(): Float = itemView.resources.displayMetrics.density * 14f

        private fun handleTouch(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    dragging = false
                    frontCard.parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!dragging && (abs(dx) > TOUCH_SLOP_PX || abs(dy) > TOUCH_SLOP_PX)) dragging = true
                    if (dragging) {
                        if (abs(dx) > abs(dy)) {
                            frontCard.translationX = dx
                            frontCard.translationY = 0f
                        } else if (dy < 0) {
                            // Upward drag is the cycling gesture — grow the real card behind
                            // into place continuously as the front card lifts away, instead of
                            // it sitting there as an inert backdrop.
                            frontCard.translationY = dy
                            frontCard.translationX = 0f
                            if (notifications.size > 1) {
                                val height = frontCard.height.takeIf { it > 0 } ?: 1
                                val progress = (-dy / height).coerceIn(0f, 1f)
                                nextCard.translationY = peekOffsetPx() * (1f - progress)
                                nextCard.scaleX = PEEK_SCALE + (1f - PEEK_SCALE) * progress
                                nextCard.scaleY = nextCard.scaleX
                            }
                        }
                        // Downward drag doesn't move the card at all — it's just closing the
                        // whole panel, so the card visually reacting alongside the panel's own
                        // collapse looked confusing.
                    }
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    frontCard.parent?.requestDisallowInterceptTouchEvent(false)
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!dragging) {
                        launch(notifications.getOrNull(currentIndex))
                        return true
                    }
                    val width = frontCard.width.takeIf { it > 0 } ?: 1
                    val height = frontCard.height.takeIf { it > 0 } ?: 1
                    when {
                        abs(dx) > abs(dy) && abs(dx) > width * 0.3f -> dismissCurrent(toRight = dx > 0)
                        dy < -height * 0.25f && abs(dy) > abs(dx) -> advance()
                        dy > height * 0.25f && abs(dy) > abs(dx) -> onSwipeDownToCollapse()
                        else -> snapBack()
                    }
                    return true
                }
            }
            return false
        }

        private fun snapBack() {
            frontCard.animate().translationX(0f).translationY(0f).setDuration(200).start()
            if (notifications.size > 1) {
                nextCard.animate()
                    .translationY(peekOffsetPx()).scaleX(PEEK_SCALE).scaleY(PEEK_SCALE)
                    .setDuration(200).start()
            }
        }

        /**
         * Flies the front card up and off while finishing the reveal of the real card behind
         * it, then swaps: the front card re-renders with the new current notification and the
         * card behind it is rebound to whatever comes after that, reset to its resting state.
         * Wraps back to the first notification once past the end, so the whole stack loops
         * rather than dead-ending.
         */
        private fun advance() {
            if (notifications.size <= 1) {
                snapBack()
                return
            }
            val nextIndex = if (currentIndex >= notifications.lastIndex) 0 else currentIndex + 1

            nextCard.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(140).start()
            frontCard.animate().translationY(-frontCard.height * 1.2f).setDuration(180).withEndAction {
                currentIndex = nextIndex
                frontCard.translationX = 0f
                frontCard.translationY = 0f
                frontCard.scaleX = 1f
                frontCard.scaleY = 1f
                renderFront()
                renderNext()
            }.start()
        }

        /** Flies the front card off toward [toRight], then cancels the underlying system notification. */
        private fun dismissCurrent(toRight: Boolean) {
            val notification = notifications.getOrNull(currentIndex) ?: return
            val exitX = if (toRight) frontCard.width * 1.3f else -frontCard.width * 1.3f
            frontCard.animate().translationX(exitX).setDuration(200)
                .withEndAction { NotificationRepository.dismiss(notification) }
                .start()
        }

        /** Fires the notification's own content intent; falls back to just opening the app if that fails or is absent. */
        private fun launch(notification: Notification?) {
            if (notification == null) return
            val intent = notification.intent
            if (intent != null) {
                try {
                    intent.send()
                    return
                } catch (e: PendingIntent.CanceledException) {
                    Log.w(TAG, "PendingIntent canceled for ${notification.packageName}, falling back to app launch")
                }
            }
            val launchIntent = context.packageManager.getLaunchIntentForPackage(notification.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            } else {
                Log.w(TAG, "No launch intent available for ${notification.packageName}")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_group, parent, false)
        return GroupViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    fun indexOfPackage(packageName: String): Int = groups.indexOfFirst { it.packageName == packageName }

    fun updateData(newGroups: List<NotificationGroup>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
