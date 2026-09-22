package com.achunt.weboslauncher

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

private const val TAG = "NotificationGroupAdapter"
private const val TOUCH_SLOP_PX = 12

/**
 * One row per app: a real card stack of that app's notifications, mirroring Palm webOS's dashboard cards.
 * - Left side: A single, stable app icon squircle with count badge (no deformed duplicate icons).
 * - Right side: A layered card stack. Underneath the front card sit decorative layered card sheets
 *   (stackLayer1, stackLayer2) providing physical depth and clear stack affordance.
 * - Gestures:
 *   - Tap               → launch notification intent (with Android 14+ BAL permission)
 *   - Slide Right       → dismiss the notification (system + local)
 *   - Slide Left        → reveal notification action intents tray (Reply, Mark as read, etc.)
 *   - Swipe Up          → cycle to the next notification in the stack
 *   - Swipe Down        → collapse the whole notification panel (delegates to [onSwipeDownToCollapse])
 */
class NotificationGroupAdapter(
    private val context: Context,
    private var groups: List<NotificationGroup>,
    private val onSwipeDownToCollapse: () -> Unit
) : RecyclerView.Adapter<NotificationGroupAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val frontCard: View = itemView.findViewById(R.id.frontCard)
        private val frontTitle: TextView = frontCard.findViewById(R.id.title)
        private val frontBody: TextView = frontCard.findViewById(R.id.body)
        private val frontDate: TextView = frontCard.findViewById(R.id.date)

        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val badgeCount: TextView = itemView.findViewById(R.id.badgeCount)

        private val stackLayer1: View = itemView.findViewById(R.id.stackLayer1)
        private val stackLayer2: View = itemView.findViewById(R.id.stackLayer2)

        private val actionsTray: LinearLayout = itemView.findViewById(R.id.actionsTray)
        private val actionsContainer: LinearLayout = itemView.findViewById(R.id.actionsContainer)

        private var notifications: List<Notification> = emptyList()
        private var currentIndex = 0

        private var downX = 0f
        private var downY = 0f
        private var initialTranslationX = 0f
        private var dragging = false
        private var isActionsRevealed = false

        private val density: Float = itemView.resources.displayMetrics.density
        private val peek1Px: Float = density * 6f
        private val peek2Px: Float = density * 12f

        init {
            frontCard.setOnTouchListener { _, event -> handleTouch(event) }
        }

        fun bind(group: NotificationGroup) {
            notifications = group.notifications
            currentIndex = 0
            isActionsRevealed = false

            resetStackTransforms()

            try {
                appIcon.setImageIcon(group.appIcon)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set app icon: ${e.message}")
            }

            renderFront()
        }

        private fun renderFront() {
            val notification = notifications.getOrNull(currentIndex) ?: return
            frontTitle.text = notification.title
            frontBody.text = notification.body
            frontDate.text = notification.date

            if (notifications.size > 1) {
                badgeCount.text = notifications.size.toString()
                badgeCount.visibility = View.VISIBLE
            } else {
                badgeCount.visibility = View.GONE
            }

            renderActions(notification)
        }

        private fun renderActions(notification: Notification) {
            actionsContainer.removeAllViews()
            val inflater = LayoutInflater.from(context)

            if (notification.actions.isNotEmpty()) {
                for (action in notification.actions) {
                    val pill = inflater.inflate(R.layout.item_notif_action_pill, actionsContainer, false) as TextView
                    pill.text = action.title
                    pill.setOnClickListener {
                        executeAction(notification, action)
                    }
                    actionsContainer.addView(pill)
                }
            } else {
                // Default actions if notification provides none
                val openPill = inflater.inflate(R.layout.item_notif_action_pill, actionsContainer, false) as TextView
                openPill.text = "Open"
                openPill.setOnClickListener {
                    launch(notification)
                }
                actionsContainer.addView(openPill)

                val dismissPill = inflater.inflate(R.layout.item_notif_action_pill, actionsContainer, false) as TextView
                dismissPill.text = "Dismiss"
                dismissPill.setOnClickListener {
                    dismissCurrent(toRight = false)
                }
                actionsContainer.addView(dismissPill)
            }
        }

        private fun resetStackTransforms() {
            frontCard.animate().cancel()
            stackLayer1.animate().cancel()
            stackLayer2.animate().cancel()
            actionsTray.animate().cancel()

            frontCard.translationX = 0f
            frontCard.translationY = 0f
            frontCard.scaleX = 1f
            frontCard.scaleY = 1f
            frontCard.alpha = 1f

            actionsTray.visibility = View.GONE
            actionsTray.alpha = 1f

            val count = notifications.size
            if (count >= 2) {
                stackLayer1.visibility = View.VISIBLE
                stackLayer1.translationY = peek1Px
                stackLayer1.scaleX = 0.96f
                stackLayer1.scaleY = 1f
                stackLayer1.alpha = 0.9f
            } else {
                stackLayer1.visibility = View.GONE
            }

            if (count >= 3) {
                stackLayer2.visibility = View.VISIBLE
                stackLayer2.translationY = peek2Px
                stackLayer2.scaleX = 0.92f
                stackLayer2.scaleY = 1f
                stackLayer2.alpha = 0.7f
            } else {
                stackLayer2.visibility = View.GONE
            }
        }

        private fun getActionsWidth(): Float {
            if (actionsContainer.width > 0) {
                return (actionsContainer.width.toFloat() + (density * 16f)).coerceAtLeast(density * 120f)
            }
            actionsContainer.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(frontCard.height.coerceAtLeast(1), View.MeasureSpec.AT_MOST)
            )
            return (actionsContainer.measuredWidth.toFloat() + (density * 16f)).coerceAtLeast(density * 120f)
        }

        private fun handleTouch(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    initialTranslationX = frontCard.translationX
                    dragging = false
                    frontCard.parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!dragging && (abs(dx) > TOUCH_SLOP_PX || abs(dy) > TOUCH_SLOP_PX)) {
                        dragging = true
                    }
                    if (dragging) {
                        if (abs(dx) > abs(dy)) {
                            // Horizontal drag
                            val targetX = initialTranslationX + dx
                            if (targetX > 0f) {
                                // Dragging right (toward dismiss)
                                frontCard.translationX = targetX
                                frontCard.translationY = 0f
                                actionsTray.visibility = View.GONE
                                stackLayer1.alpha = 0.9f
                                stackLayer2.alpha = 0.7f
                            } else {
                                // Dragging left (toward revealing action intents)
                                frontCard.translationX = targetX.coerceAtLeast(-frontCard.width * 0.85f)
                                frontCard.translationY = 0f
                                actionsTray.visibility = View.VISIBLE
                                val actionsW = getActionsWidth()
                                val progress = (-targetX / (actionsW * 0.5f)).coerceIn(0f, 1f)
                                stackLayer1.alpha = 0.9f * (1f - progress)
                                stackLayer2.alpha = 0.7f * (1f - progress)
                            }
                        } else if (dy < 0) {
                            // Upward drag is cycling gesture
                            frontCard.translationY = dy
                            frontCard.translationX = 0f
                            if (notifications.size > 1) {
                                val height = frontCard.height.takeIf { it > 0 } ?: 1
                                val progress = (-dy / height).coerceIn(0f, 1f)
                                stackLayer1.translationY = peek1Px * (1f - progress)
                                stackLayer1.scaleX = 0.96f + 0.04f * progress
                                stackLayer1.alpha = 0.9f + 0.1f * progress
                                if (notifications.size >= 3) {
                                    stackLayer2.translationY = peek2Px - (peek2Px - peek1Px) * progress
                                    stackLayer2.scaleX = 0.92f + 0.04f * progress
                                    stackLayer2.alpha = 0.7f + 0.2f * progress
                                }
                            }
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    frontCard.parent?.requestDisallowInterceptTouchEvent(false)
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY

                    if (!dragging) {
                        if (isActionsRevealed) {
                            snapBack()
                        } else {
                            launch(notifications.getOrNull(currentIndex))
                        }
                        return true
                    }

                    val width = frontCard.width.takeIf { it > 0 } ?: 1
                    val height = frontCard.height.takeIf { it > 0 } ?: 1

                    if (abs(dx) > abs(dy)) {
                        if (!isActionsRevealed) {
                            when {
                                dx > width * 0.35f -> dismissCurrent(toRight = true)
                                dx < -getActionsWidth() * 0.35f -> snapOpenActions()
                                else -> snapBack()
                            }
                        } else {
                            if (dx > getActionsWidth() * 0.3f) {
                                snapBack()
                            } else {
                                snapOpenActions()
                            }
                        }
                    } else {
                        when {
                            dy < -height * 0.25f -> advance()
                            dy > height * 0.25f -> onSwipeDownToCollapse()
                            else -> snapBack()
                        }
                    }
                    return true
                }
            }
            return false
        }

        private fun snapBack() {
            isActionsRevealed = false
            frontCard.animate()
                .translationX(0f)
                .translationY(0f)
                .alpha(1f)
                .setDuration(200)
                .start()

            actionsTray.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    actionsTray.visibility = View.GONE
                    actionsTray.alpha = 1f
                }
                .start()

            if (notifications.size >= 2) {
                stackLayer1.animate()
                    .translationY(peek1Px)
                    .scaleX(0.96f)
                    .alpha(0.9f)
                    .setDuration(200)
                    .start()
            }
            if (notifications.size >= 3) {
                stackLayer2.animate()
                    .translationY(peek2Px)
                    .scaleX(0.92f)
                    .alpha(0.7f)
                    .setDuration(200)
                    .start()
            }
        }

        private fun snapOpenActions() {
            isActionsRevealed = true
            val openX = -getActionsWidth()
            actionsTray.visibility = View.VISIBLE
            actionsTray.alpha = 1f
            frontCard.animate()
                .translationX(openX)
                .translationY(0f)
                .alpha(1f)
                .setDuration(200)
                .start()

            stackLayer1.animate().alpha(0f).setDuration(150).start()
            stackLayer2.animate().alpha(0f).setDuration(150).start()
        }

        private fun advance() {
            if (notifications.size <= 1) {
                snapBack()
                return
            }
            val nextIndex = if (currentIndex >= notifications.lastIndex) 0 else currentIndex + 1

            stackLayer1.animate()
                .translationY(0f)
                .scaleX(1f)
                .alpha(1f)
                .setDuration(160)
                .start()

            frontCard.animate()
                .translationY(-frontCard.height * 1.2f)
                .alpha(0f)
                .setDuration(180)
                .withEndAction {
                    currentIndex = nextIndex
                    resetStackTransforms()
                    renderFront()
                }
                .start()
        }

        private fun dismissCurrent(toRight: Boolean) {
            val notification = notifications.getOrNull(currentIndex) ?: return
            val exitX = if (toRight) frontCard.width * 1.3f else -frontCard.width * 1.3f

            if (notifications.size > 1) {
                stackLayer1.animate()
                    .translationY(0f)
                    .scaleX(1f)
                    .alpha(1f)
                    .setDuration(180)
                    .start()
            }

            frontCard.animate()
                .translationX(exitX)
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    NotificationRepository.dismiss(notification)
                }
                .start()
        }

        private fun executeAction(notification: Notification, action: NotificationAction) {
            val intent = action.intent
            if (intent != null) {
                val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ActivityOptions.makeBasic().apply {
                        pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    }.toBundle()
                } else {
                    ActivityOptions.makeBasic().toBundle()
                }
                try {
                    intent.send(context, 0, null, null, null, null, options)
                    Log.d(TAG, "Sent action intent for ${action.title}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to send action PendingIntent: ${e.message}", e)
                }
            }
            snapBack()
        }

        private fun launch(notification: Notification?) {
            if (notification == null) return
            val intent = notification.intent
            var launched = false
            if (intent != null) {
                val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ActivityOptions.makeBasic().apply {
                        pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    }.toBundle()
                } else {
                    ActivityOptions.makeBasic().toBundle()
                }
                try {
                    intent.send(context, 0, null, null, null, null, options)
                    launched = true
                    Log.d(TAG, "Successfully sent notification PendingIntent for ${notification.packageName}")
                } catch (e: Exception) {
                    Log.w(TAG, "PendingIntent failed for ${notification.packageName}: ${e.message}", e)
                }
            }
            if (!launched) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(notification.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    launched = true
                    Log.d(TAG, "Launched app via package manager for ${notification.packageName}")
                } else {
                    Log.w(TAG, "No launch intent available for ${notification.packageName}")
                }
            }

            if (launched) {
                if (notification.isAutoCancel) {
                    NotificationRepository.dismiss(notification)
                }
                (context as? MainActivity)?.collapseNotificationPanel()
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
