package com.achunt.weboslauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationFragment : Fragment() {

    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationRecyclerViewSmall: RecyclerView
    private lateinit var notificationAdapter: NotificationItemAdapter
    private lateinit var notificationIconAdapter: NotificationItemIconAdapter
    private var readyListener: OnNotificationsReadyListener? = null

    private var isExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView)
        notificationRecyclerViewSmall = view.findViewById(R.id.notificationRecyclerViewSmall)

        notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationRecyclerViewSmall.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, true)

        notificationAdapter = NotificationItemAdapter(requireContext(), NotificationRepository.getNotifications())
        notificationIconAdapter = NotificationItemIconAdapter(
            requireContext(),
            NotificationRepository.getNotificationsIcons()
        ) { iconClicked ->
            isExpanded = true
            // Expand logic: scroll to the first matching item
            val notifications = NotificationRepository.getNotifications()
            val matchIndex = notifications.indexOfFirst { it.appIcon == iconClicked }
            if (matchIndex != -1) {
                notificationRecyclerView.scrollToPosition(matchIndex)
                notificationAdapter.updateData(NotificationRepository.getNotifications())

            }
            notificationRecyclerView.visibility = View.VISIBLE
        }
        readyListener?.onNotificationsReady()

        notificationRecyclerView.adapter = notificationAdapter
        notificationRecyclerViewSmall.adapter = notificationIconAdapter

        NotificationRepository.getNotificationLiveData().observe(viewLifecycleOwner) { notifications ->
            notificationAdapter.updateData(notifications)
            (activity as? MainActivity)?.checkAndSetNotificationVisibility()
        }


        NotificationRepository.getNotificationIconLiveData().observe(viewLifecycleOwner) { icons ->
            notificationIconAdapter.updateData(icons)
            (activity as? MainActivity)?.checkAndSetNotificationVisibility()
        }

        // Click small icons row to toggle expanded view
        notificationRecyclerViewSmall.setOnClickListener {
            toggleView()
        }

        // Click full list to collapse
        notificationRecyclerView.setOnClickListener {
            toggleView()
        }

        // Start collapsed
        setExpanded(false, immediate = true)

        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val notification = NotificationRepository.getNotifications()[position]
                NotificationRepository.removeNotification(notification)
            }
        }

        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(notificationRecyclerView)

        return view
    }

    private fun toggleView() {
        isExpanded = !isExpanded
        setExpanded(isExpanded)
    }

    private fun setExpanded(expand: Boolean, immediate: Boolean = false) {
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = if (immediate) 0 else 300 }
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = if (immediate) 0 else 300 }

        if (expand) {
            notificationRecyclerViewSmall.startAnimation(fadeOut)
            notificationRecyclerView.startAnimation(fadeIn)
            notificationRecyclerViewSmall.visibility = View.VISIBLE
            notificationRecyclerView.visibility = View.VISIBLE
        } else {
            notificationRecyclerView.startAnimation(fadeOut)
            notificationRecyclerViewSmall.startAnimation(fadeIn)
            notificationRecyclerView.visibility = View.VISIBLE
            notificationRecyclerViewSmall.visibility = View.VISIBLE
        }
    }
    fun refreshView() {
        notificationAdapter.notifyDataSetChanged()
        notificationIconAdapter.notifyDataSetChanged()
        (activity as? MainActivity)?.checkAndSetNotificationVisibility()

    }

    fun hasNotifications(): Boolean {
        return this::notificationIconAdapter.isInitialized && notificationIconAdapter.itemCount > 0
    }


    fun setOnNotificationsReadyListener(listener: OnNotificationsReadyListener) {
        readyListener = listener
    }


}

