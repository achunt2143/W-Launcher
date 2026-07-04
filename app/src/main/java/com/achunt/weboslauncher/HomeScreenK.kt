package com.achunt.weboslauncher

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeScreenK : Fragment(),
    DockAdapter.DockInteractionListener {

    // Dock
    private lateinit var dockRecycler: RecyclerView
    private lateinit var dockAdapter: DockAdapter
    private lateinit var dockRepository: DockRepository
    private lateinit var dockTouchHelper: ItemTouchHelper

    lateinit var widgets: LinearLayout
    lateinit var recents: RecyclerView
    lateinit var sharedPrefH: SharedPreferences

    // recentsList(...) can run more than once (it's re-invoked once the async app list
    // finishes loading if it wasn't ready on the first pass) — these are set up once and
    // reused rather than recreated on every call, since RecyclerView throws if a second
    // SnapHelper is attached without detaching the first.
    private var recentsSnapHelper: LinearSnapHelper? = null
    private var recentsSwipeHelper: ItemTouchHelper? = null
    var apps: List<AppInfo>? = null
    var appsToPass: List<ResolveInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val start = System.currentTimeMillis()
        // Use viewLifecycleOwner scope via lifecycleScope on the fragment itself —
        // safe because onCreate fires before view creation but the fragment lifecycle
        // is still valid here. IO work + Main dispatch avoids race conditions.
        lifecycleScope.launch(Dispatchers.IO) {
            val adapter = RAdapter(requireContext())
            withContext(Dispatchers.Main) {
                apps = adapter.appsList
                appsToPass = adapter.resolveList
                adapterSystem = RAdapterSystem(requireContext(), appsToPass)
                adapterDownloads = RAdapterDownloads(requireContext(), appsToPass)
                adapterSettings = RAdapterSettings(requireContext())
                adapterWork = RAdapterWork(requireContext())
                val endFind = System.currentTimeMillis()
                Log.d("App Finder", "Time to call RAdapter: " + (endFind - start))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.homescreen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        widgets = view.findViewById(R.id.widgets)
        recents = view.findViewById(R.id.recents)
        dockRecycler = view.findViewById(R.id.dock)
        sharedPrefH = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val recentsT = sharedPrefH.getBoolean("recents", false)

        // ---- Dock setup ----
        dockRepository = DockRepository(requireContext())
        if (dockRepository.isEmpty()) {
            dockRepository.seedFromLegacy()
        }
        val dockLayoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        ).apply { stackFromEnd = false }
        dockRecycler.layoutManager = dockLayoutManager
        dockRecycler.itemAnimator = DefaultItemAnimator()
        dockAdapter = DockAdapter(
            requireContext(),
            dockRepository.getDockItems().toMutableList(),
            this
        )
        dockRecycler.adapter = dockAdapter
        dockTouchHelper = ItemTouchHelper(
            DockTouchHelperCallback(
                adapter = dockAdapter,
                onReorderFinished = { from, to ->
                    if (from != to) dockRepository.moveItem(from, to)
                },
                onLongPressWithoutMove = { position, itemView ->
                    dockAdapter.getItem(position)?.let {
                        AppActionsMenu.show(itemView, it.packageName, AppActionsMenu.Source.DOCK)
                    }
                }
            )
        )
        dockTouchHelper.attachToRecyclerView(dockRecycler)
        applyDockTheme()

        // ---- Entry animations ----
        view.post {
            val animationDuration = 500L
            animateDock(animationDuration)
            if (recentsT) {
                launchWithDelay(500) { recentsList(requireContext()) }
            }
        }

        val w = requireActivity().window
        w.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.empty)
        widgets.animate().alpha(1f).setDuration(1000).start()

        lifecycleScope.launch(Dispatchers.Default) {
            val mAppWidgetManager = AppWidgetManager.getInstance(view.context)
            val mAppWidgetHost = AppWidgetHost(view.context, APPWIDGET_HOST_ID)
            try {
                createWidget(
                    view,
                    "com.achunt.justtype",
                    "com.achunt.justtype.JustTypeWidget",
                    mAppWidgetHost,
                    mAppWidgetManager
                )
            } catch (e: Exception) {
                Log.d("JTError", e.toString())
            }
        }
    }

    // ---- DockInteractionListener ----

    override fun onDockAppClicked(item: DockItem) {
        goodbyeList.remove(item.packageName)
        val launchIntent = requireContext().packageManager
            .getLaunchIntentForPackage(item.packageName)
        if (launchIntent != null) startActivity(launchIntent)
    }

    override fun onDockDrawerClicked() {
        // The drawer button is part of the dock, which now stays visible and tappable even
        // while AppsDrawer is open on top of this fragment (see loadFragment/AppsDrawer) — so
        // without this check, tapping it again while already open just added another AppsDrawer
        // instance on top of the last one instead of closing it.
        if (parentFragmentManager.findFragmentByTag("apps") != null) {
            parentFragmentManager.popBackStack()
        } else {
            widgets.animate().alpha(0f).setDuration(1000).start()
            loadFragment(AppsDrawer())
        }
    }

    override fun onDockDragRequested(viewHolder: RecyclerView.ViewHolder) {
        dockTouchHelper.startDrag(viewHolder)
    }

    // ---- Public API: called from AppActionsMenu's long-press popup ----

    fun addToDock(packageName: String) {
        val added = dockRepository.addItem(packageName)
        if (added) {
            dockAdapter.updateItems(dockRepository.getDockItems())
        } else {
            val pm = requireContext().packageManager
            val label = try {
                pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
            } catch (e: PackageManager.NameNotFoundException) { packageName }
            AlertDialog.Builder(requireContext())
                .setMessage("$label is already in the dock, or the dock is full (max ${DockRepository.MAX_DOCK_ITEMS}).")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    fun removeFromDock(packageName: String) {
        dockRepository.removeItem(packageName)
        dockAdapter.updateItems(dockRepository.getDockItems())
    }

    // ---- Fragment loading ----

    fun launchWithDelay(delayMillis: Long, action: () -> Unit) {
        lifecycleScope.launch {
            delay(delayMillis)
            action.invoke()
        }
    }

    fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            if (fragment is AppsDrawer) {
                AppsDrawer.pendingBackdrop = captureFrostedBackdrop()
            }
            fragment.retainInstance = true
            fragment.enterTransition = Slide(Gravity.BOTTOM)
            fragment.exitTransition = Slide(Gravity.BOTTOM)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(R.id.container, fragment, "apps")
                .addToBackStack("home")
                .commit()
            return true
        }
        return false
    }

    /**
     * Snapshots the home screen (recents + dock, still fully visible at this point since
     * the drawer hasn't been added yet) at a heavily downscaled size. Upscaling this back
     * up in AppsDrawer is a cheap, dependency-free stand-in for a real Gaussian blur — it
     * smears the recents cards/dock icons into soft blobs instead of legible shapes, which
     * is what lets AppsDrawer's frosted-glass background read as intentional instead of
     * the old raw-transparency ghosting.
     */
    private fun captureFrostedBackdrop(): Bitmap? {
        val container = requireActivity().findViewById<View>(R.id.container)
        if (container.width == 0 || container.height == 0) return null
        val targetWidth = 24
        val scale = targetWidth.toFloat() / container.width
        val targetHeight = (container.height * scale).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            scale(scale, scale)
            container.draw(this)
        }
        return bitmap
    }

    // ---- Dock animation (replaces per-ImageView animation) ----

    /** Slides the dock up into view on first entry. The dock otherwise stays put — including
     * while AppsDrawer is open on top of it, since the drawer now leaves room for it. */
    private fun animateDock(duration: Long) {
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        dockRecycler.visibility = View.VISIBLE
        dockRecycler.translationY = screenHeight
        dockRecycler.animate().translationY(0f).setDuration(duration).start()
    }

    // ---- Theme helper ----

    private fun applyDockTheme() {
        dockRecycler.setBackgroundResource(ThemePreference.dockBackgroundRes(requireContext()))
    }

    // ---- Widget helper ----

    suspend fun createWidget(
        view: View,
        packageName: String,
        className: String,
        mAppWidgetHost: AppWidgetHost,
        mAppWidgetManager: AppWidgetManager
    ): Boolean = withContext(Dispatchers.IO) {
        var newAppWidgetProviderInfo: AppWidgetProviderInfo? = null
        val appWidgetInfos = mAppWidgetManager.installedProviders
        var widgetIsFound = false
        for (j in appWidgetInfos.indices) {
            if (appWidgetInfos[j].provider.packageName == packageName
                && appWidgetInfos[j].provider.className == className) {
                newAppWidgetProviderInfo = appWidgetInfos[j]
                widgetIsFound = true
                break
            }
        }
        return@withContext if (!widgetIsFound) {
            false
        } else {
            val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
            val hostView = mAppWidgetHost.createView(view.context, appWidgetId, newAppWidgetProviderInfo)
            hostView.setAppWidget(appWidgetId, newAppWidgetProviderInfo)

            val widgetLayout = view.findViewById<LinearLayout>(R.id.widgets)
            widgetLayout.addView(hostView)

            val allowed = mAppWidgetManager.bindAppWidgetIdIfAllowed(
                appWidgetId, newAppWidgetProviderInfo!!.provider
            )
            if (!allowed) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, newAppWidgetProviderInfo.provider)
                startActivityForResult(intent, 200906)
            }
            true
        }
    }

    // ---- Recents ----

    fun recentsList(context: Context) {
        val start = System.currentTimeMillis()
        val sharedPrefH1 = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (sharedPrefH1.getBoolean("recents", false)) {
            try {
                if (!recentsList.isEmpty()) recentsList = mutableListOf()
                recents.itemAnimator = DefaultItemAnimator()
                val usm = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                // Query window expanded to match the 10-minute filter below
                val aslist = usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    time - 600000, time
                ).toMutableList()

                // Oldest first, newest last — so as cards are appended below, the oldest
                // still-open app lands at the left end of the deck and each newer one takes
                // its place further right, same as webOS's card stack.
                appStatsList = aslist.sortedBy { it.lastTimeUsed } as MutableList<UsageStats>
                appStatsList.forEach { asl ->
                    if (asl.lastTimeUsed > time - 600000) {
                        apps?.forEach { app ->
                            if (app.packageName == asl.packageName) {
                                if (!usm.isAppInactive(asl.packageName)
                                    && asl.packageName != "com.achunt.weboslauncher"
                                    && asl.packageName != "com.achunt.justtype"
                                    && !goodbyeList.contains(asl.packageName)) {
                                    recentsList.add(app)
                                    Log.d("Recents", app.packageName.toString())
                                }
                            }
                        }
                    }
                }

                recents.layoutManager = LinearLayoutManager(
                    requireContext(), LinearLayoutManager.HORIZONTAL, false
                )
                recentsAdapter = RecentsAdapter(recentsList)
                recents.adapter = recentsAdapter

                // Snap card-to-card like webOS's card carousel instead of resting wherever
                // a scroll happens to stop. Created once and reused — attaching a second
                // SnapHelper without detaching the first throws.
                if (recentsSnapHelper == null) {
                    recentsSnapHelper = LinearSnapHelper().also { it.attachToRecyclerView(recents) }
                }

                // Side padding equal to half the leftover width lets the first and last
                // cards snap to the same centered position as every card in between,
                // instead of stopping flush against the RecyclerView's edge.
                recents.post {
                    val cardSlotWidthPx = resources.getDimensionPixelSize(R.dimen.recents_card_slot_width)
                    val sidePadding = ((recents.width - cardSlotWidthPx) / 2).coerceAtLeast(0)
                    recents.clipToPadding = false
                    recents.setPadding(sidePadding, recents.paddingTop, sidePadding, recents.paddingBottom)

                    // Open on the most recently used app — the last (rightmost) card —
                    // instead of defaulting to the oldest one at the far left. The
                    // RecyclerView is freshly laid out at scrollX=0 with the first card
                    // already centered (thanks to the padding above), so shifting over by
                    // (count-1) card-widths lands exactly on the last card, centered too.
                    if (recentsList.size > 1) {
                        recents.scrollBy((recentsList.size - 1) * cardSlotWidthPx, 0)
                    }
                }

                // Flick a card up to close it — kills the app immediately, same as the
                // long-press path, but removes it from the carousel right away since
                // ItemTouchHelper's own fling animation already carries it off-screen.
                // Also created once and reused, for the same reason as the SnapHelper above.
                if (recentsSwipeHelper == null) {
                    recentsSwipeHelper = ItemTouchHelper(RecentsSwipeToDismissCallback { position ->
                        if (position == RecyclerView.NO_POSITION || position !in recentsList.indices) return@RecentsSwipeToDismissCallback
                        killAndForget(context, recentsList[position].packageName as String)
                        removeRecentCard(position)
                    }).also { it.attachToRecyclerView(recents) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val endFind = System.currentTimeMillis()
        Log.d("Recents Finder", "to call Recents " + (endFind - start))
    }


    // ---- Recents click listeners ----

    class RecentsClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val recyclerView = v.rootView.findViewById<RecyclerView>(R.id.recents)
            // getChildAdapterPosition replaces deprecated getChildPosition
            val selectedItemPosition = recyclerView.getChildAdapterPosition(v)
            if (selectedItemPosition == RecyclerView.NO_POSITION) return
            v.context.startActivity(
                v.context.packageManager.getLaunchIntentForPackage(
                    recentsList[selectedItemPosition].packageName as String
                )
            )
        }
    }

    class RecentsLongClickListener : View.OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            val recyclerView = v.rootView.findViewById<RecyclerView>(R.id.recents)
            val selectedItemPosition = recyclerView.getChildAdapterPosition(v)
            if (selectedItemPosition == RecyclerView.NO_POSITION) return false
            killAndForget(v.context, recentsList[selectedItemPosition].packageName as String)
            // Handler with explicit Looper — replaces deprecated no-arg Handler()
            Handler(Looper.getMainLooper()).postDelayed({
                removeRecentCard(selectedItemPosition)
            }, 500)
            return true
        }
    }

    companion object {
        @Volatile lateinit var adapter: RAdapter
        @Volatile lateinit var adapterSystem: RecyclerView.Adapter<*>
        @Volatile lateinit var adapterDownloads: RecyclerView.Adapter<*>
        @Volatile lateinit var adapterSettings: RecyclerView.Adapter<*>
        @Volatile lateinit var adapterWork: RecyclerView.Adapter<*>
        const val APPWIDGET_HOST_ID = 200906
        lateinit var appStatsList: MutableList<UsageStats>
        lateinit var recentsAdapter: RecentsAdapter
        var recentsList = mutableListOf<AppInfo>()
        var goodbyeList = mutableSetOf<String>()
        var isHasWorkApps = false

        /** Kills the app's background process and marks it so it won't reappear in recents. */
        fun killAndForget(context: Context, packageName: String) {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.killBackgroundProcesses(packageName)
            goodbyeList.add(packageName)
        }

        /** Removes the card at [position] from the recents list and adapter. */
        fun removeRecentCard(position: Int) {
            if (position == RecyclerView.NO_POSITION || position !in recentsList.indices) return
            recentsList.removeAt(position)
            recentsAdapter.notifyItemRemoved(position)
        }
    }
}
