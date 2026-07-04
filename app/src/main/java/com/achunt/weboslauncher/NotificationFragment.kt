package com.achunt.weboslauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationFragment : Fragment() {

    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationRecyclerViewSmall: RecyclerView
    private lateinit var groupAdapter: NotificationGroupAdapter
    private lateinit var notificationIconAdapter: NotificationItemIconAdapter
    private var readyListener: OnNotificationsReadyListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView)
        notificationRecyclerViewSmall = view.findViewById(R.id.notificationRecyclerViewSmall)

        notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // reverseLayout=true so icons are right-aligned with the oldest notification at the
        // rightmost position — new ones are appended to the end of the data list but visually
        // land to the left of it, growing the cluster leftward as it accumulates.
        notificationRecyclerViewSmall.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, true)

        val initialGroups = NotificationRepository.getNotifications().grouped()
        groupAdapter = NotificationGroupAdapter(requireContext(), initialGroups) {
            (activity as? MainActivity)?.collapseNotificationPanel()
        }
        notificationIconAdapter = NotificationItemIconAdapter(
            requireContext(),
            initialGroups
        ) { groupClicked ->
            val matchIndex = groupAdapter.indexOfPackage(groupClicked.packageName)
            (activity as? MainActivity)?.expandNotificationPanel()
            if (matchIndex != -1) {
                notificationRecyclerView.post { notificationRecyclerView.scrollToPosition(matchIndex) }
            }
        }
        readyListener?.onNotificationsReady()

        notificationRecyclerView.adapter = groupAdapter
        notificationRecyclerViewSmall.adapter = notificationIconAdapter

        NotificationRepository.getNotificationLiveData().observe(viewLifecycleOwner) { notifications ->
            val groups = notifications.grouped()
            groupAdapter.updateData(groups)
            notificationIconAdapter.updateData(groups)
            (activity as? MainActivity)?.checkAndSetNotificationVisibility()
            (activity as? MainActivity)?.refreshNotificationPanelHeight()
        }

        // Tap the collapsed icon row to expand; tap the expanded list's background to collapse.
        notificationRecyclerViewSmall.setOnClickListener {
            (activity as? MainActivity)?.toggleNotification()
        }
        notificationRecyclerView.setOnClickListener {
            (activity as? MainActivity)?.toggleNotification()
        }

        // Start collapsed
        applyExpandedState(false, immediate = false)

        return view
    }

    /**
     * Shows the full swipeable card list when [expand], otherwise just the collapsed icon row.
     * Both directions slide rather than fade: expanding slides the icon row up and out while
     * the list slides up into place from just below it (like a drawer opening); collapsing is
     * the same motion in reverse.
     */
    @JvmOverloads
    fun applyExpandedState(expand: Boolean, immediate: Boolean = false) {
        val shownView = if (expand) notificationRecyclerView else notificationRecyclerViewSmall
        val hiddenView = if (expand) notificationRecyclerViewSmall else notificationRecyclerView

        if (immediate) {
            hiddenView.visibility = View.GONE
            hiddenView.translationY = 0f
            shownView.visibility = View.VISIBLE
            shownView.translationY = 0f
            return
        }

        val slideDistance = resources.displayMetrics.density * 40f
        val enterFrom = if (expand) slideDistance else -slideDistance
        val exitTo = if (expand) -slideDistance else slideDistance

        shownView.translationY = enterFrom
        shownView.visibility = View.VISIBLE
        shownView.animate().translationY(0f).setDuration(220).start()

        hiddenView.animate().translationY(exitTo).setDuration(220).withEndAction {
            hiddenView.visibility = View.GONE
            hiddenView.translationY = 0f
        }.start()
    }

    /**
     * Measures how tall the panel needs to be for [expand]'s target row alone. Both rows stay
     * VISIBLE together for the duration of [applyExpandedState]'s cross-slide, so measuring the
     * whole LinearLayout mid-transition sums both rows' heights and hands MainActivity's panel
     * height animator an inflated target — which then has to correct itself once the old row
     * actually goes GONE, visible as the dock jumping into place a beat late.
     */
    fun computeTargetHeight(expand: Boolean, width: Int): Int {
        if (!this::notificationRecyclerView.isInitialized) return 0
        val root = notificationRecyclerView.parent as? View ?: return 0
        val other = if (expand) notificationRecyclerViewSmall else notificationRecyclerView
        val otherWasVisible = other.visibility
        other.visibility = View.GONE
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        root.measure(widthSpec, heightSpec)
        val measured = root.measuredHeight
        other.visibility = otherWasVisible
        return measured
    }

    fun hasNotifications(): Boolean {
        return this::notificationIconAdapter.isInitialized && notificationIconAdapter.itemCount > 0
    }

    fun setOnNotificationsReadyListener(listener: OnNotificationsReadyListener) {
        readyListener = listener
    }
}
