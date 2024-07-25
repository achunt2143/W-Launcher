package com.achunt.weboslauncher

// NotificationFragment.kt
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
    private lateinit var notificationAdapter: NotificationItemAdapter
    private lateinit var notificationIconAdapter: NotificationItemIconAdapter
    private var isExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        // Initialize RecyclerView
        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView)
        notificationRecyclerViewSmall = view.findViewById(R.id.notificationRecyclerViewSmall)
        notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationRecyclerViewSmall.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, true)
        // Set up adapter
        notificationAdapter =
            NotificationItemAdapter(requireContext(), NotificationRepository.getNotifications())
        notificationIconAdapter = NotificationItemIconAdapter(
            requireContext(),
            NotificationRepository.getNotificationsIcons()
        )


        NotificationRepository.getNotificationLiveData()
            .observe(viewLifecycleOwner) { notifications ->
                notificationAdapter.updateData(notifications)
            }
        NotificationRepository.getNotificationIconLiveData()
            .observe(viewLifecycleOwner) { notificationsIcons ->
                notificationIconAdapter.updateData(notificationsIcons)
            }
        notificationRecyclerView.adapter = notificationAdapter
        notificationRecyclerViewSmall.adapter = notificationIconAdapter

        println("done creating view")

        return view
    }
}

