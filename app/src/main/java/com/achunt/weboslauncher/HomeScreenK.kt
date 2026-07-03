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
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeScreenK : Fragment(),
    FragmentManager.OnBackStackChangedListener,
    DockAdapter.DockInteractionListener {

    // Dock
    private lateinit var dockRecycler: RecyclerView
    private lateinit var dockAdapter: DockAdapter
    private lateinit var dockRepository: DockRepository

    lateinit var widgets: LinearLayout
    lateinit var recents: RecyclerView
    lateinit var sharedPrefH: SharedPreferences
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
        val theme = sharedPrefH.getString("themeName", "Classic")
        val recentsT = sharedPrefH.getBoolean("recents", false)
        parentFragmentManager.addOnBackStackChangedListener(this)

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
        applyDockTheme(theme)

        // ---- Entry animations ----
        view.post {
            val animationDuration = 500L
            animateDock(animationDuration, false)
            if (recentsT) {
                launchWithDelay(500) { recentsList(requireContext()) }
            }
        }

        val w = requireActivity().window
        w.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.empty)
        widgets.animate().alpha(1f).setDuration(1000).start()


        when (theme) {
            "Classic" -> {
                imageViewPhone.setImageResource(R.drawable.phone)
                imageViewContacts.setImageResource(R.drawable.cnt)
                imageViewMessages.setImageResource(R.drawable.msg)
                imageViewBrowser.setImageResource(R.drawable.brs)
            }

            "Classic3" -> {
                imageViewPhone.setImageResource(R.drawable.phone)
                imageViewContacts.setImageResource(R.drawable.cnt)
                imageViewMessages.setImageResource(R.drawable.msg)
                imageViewBrowser.setImageResource(R.drawable.brs)
            }
            "Mochi" -> {
                imageViewPhone.setImageResource(R.drawable.mochiphone)
                imageViewContacts.setImageResource(R.drawable.mochicontacts)
                imageViewMessages.setImageResource(R.drawable.mochimessages)
                imageViewBrowser.setImageResource(R.drawable.mochibrowser)
                gridDock.background =
                    context?.let { AppCompatResources.getDrawable(it, R.color.mochilight) }
            }
            "Modern" -> {
                imageViewPhone.setImageResource(R.drawable.modernphone)
                imageViewContacts.setImageResource(R.drawable.moderncontact)
                imageViewMessages.setImageResource(R.drawable.modernmessages)
                imageViewBrowser.setImageResource(R.drawable.modernbrowser)
                gridDock.background =
                    context?.let { AppCompatResources.getDrawable(it, R.drawable.modern_dock) }
            }
            "System" -> {
                val browserIntent = Intent("android.intent.action.VIEW", Uri.parse("http://"))
                val resolveBrowserInfo = view.context.packageManager.resolveActivity(
                    browserIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                val phoneNumber = "1234567890"
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                val resolvePhoneInfo = view.context.packageManager.resolveActivity(
                    dialIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                val contactsIntent = Intent(Intent.ACTION_VIEW)
                contactsIntent.data = ContactsContract.Contacts.CONTENT_URI
                val resolveContactsInfo = view.context.packageManager.resolveActivity(
                    contactsIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                val smsUri = Uri.parse("smsto:$phoneNumber")
                val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)
                val resolveSmsInfo = view.context.packageManager.resolveActivity(
                    smsIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                if (resolvePhoneInfo != null) {
                    imageViewPhone.setImageDrawable(
                        resolvePhoneInfo.activityInfo.applicationInfo.loadIcon(
                            requireContext().packageManager
                        )
                    )
                } else {
                    imageViewPhone.setImageResource(R.drawable.phone)
                }
                if (resolveContactsInfo != null) {
                    imageViewContacts.setImageDrawable(
                        resolveContactsInfo.activityInfo.applicationInfo.loadIcon(
                            requireContext().packageManager
                        )
                    )
                } else {
                    imageViewContacts.setImageResource(R.drawable.cnt)
                }
                if (resolveSmsInfo != null) {
                    imageViewMessages.setImageDrawable(
                        resolveSmsInfo.activityInfo.applicationInfo.loadIcon(
                            requireContext().packageManager
                        )
                    )
                } else {
                    imageViewMessages.setImageResource(R.drawable.msg)
                }
                if (resolveBrowserInfo != null) {
                    imageViewBrowser.setImageDrawable(
                        resolveBrowserInfo.activityInfo.applicationInfo.loadIcon(
                            requireContext().packageManager
                        )
                    )
                } else {
                    imageViewBrowser.setImageResource(R.drawable.brs)
                }
                gridDock.background =
                    context?.let { AppCompatResources.getDrawable(it, R.color.abt) }
            }
        }

        val sharedPrefs: SharedPreferences =
            view.context.getSharedPreferences("SpecialApps", Context.MODE_PRIVATE)
        imageViewDrawer.setOnClickListener {
            widgets.animate().alpha(0f).setDuration(1000).start()
            view.post {
                val animationDuration = 500L
                animateImageViewTranslation(imageViewDrawer, animationDuration, true)
                animateImageViewTranslation(imageViewPhone, animationDuration, true)
                animateImageViewTranslation(imageViewContacts, animationDuration, true)
                animateImageViewTranslation(imageViewMessages, animationDuration, true)
                animateImageViewTranslation(imageViewBrowser, animationDuration, true)
            }
            loadFragment(AppsDrawer())
        }
        imageViewPhone.setOnClickListener { v: View ->
            val context = v.context
            val phonePackageName = sharedPrefs.getString("PhonePackageName", null)
            if (phonePackageName != null) {
                goodbyeList.remove(phonePackageName)
                val launchIntent =
                    context.packageManager.getLaunchIntentForPackage(phonePackageName)
                context.startActivity(launchIntent)
            } else {
                val intent = Intent(Intent.ACTION_DIAL)
                context.startActivity(intent)
            }
        }

        imageViewContacts.setOnClickListener { v: View ->
            val context = v.context
            val contactsPackageName = sharedPrefs.getString("ContactsPackageName", null)
            if (contactsPackageName != null) {
                goodbyeList.remove(contactsPackageName)
                val launchIntent =
                    context.packageManager.getLaunchIntentForPackage(contactsPackageName)
                context.startActivity(launchIntent)
            }
        }

        imageViewMessages.setOnClickListener { v: View ->
            val context = v.context
            val messagesPackageName = sharedPrefs.getString("MessagesPackageName", null)
            if (messagesPackageName != null) {
                goodbyeList.remove(messagesPackageName)
                val launchIntent =
                    context.packageManager.getLaunchIntentForPackage(messagesPackageName)
                context.startActivity(launchIntent)
            }
        }

        imageViewBrowser.setOnClickListener {
            val browser = Intent(Intent.ACTION_MAIN)
            browser.addCategory(Intent.CATEGORY_APP_BROWSER)
            val mainLauncherList = context?.packageManager?.queryIntentActivities(browser, 0)
            if (mainLauncherList != null) {
                goodbyeList.remove(mainLauncherList.first().activityInfo.packageName)
            }
            startActivity(browser)
        }


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
        widgets.animate().alpha(0f).setDuration(1000).start()
        view?.post {
            animateDock(500L, true)
        }
        loadFragment(AppsDrawer())
    }

    override fun onDockItemLongPressed(item: DockItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(item.label)
            .setItems(arrayOf("Remove from dock")) { _, _ ->
                dockRepository.removeItem(item.packageName)
                dockAdapter.updateItems(dockRepository.getDockItems())
            }
            .show()
    }

    // ---- Public API: called from RAdapterSystem / RAdapterDownloads long-press ----

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

    // ---- Fragment back-stack listener ----

    override fun onBackStackChanged() {
        val currentFragment = parentFragmentManager.findFragmentById(R.id.container)
        val isAppsDrawerFragmentVisible = currentFragment is AppsDrawer
        val animationDuration = 500L
        animateImageViewTranslation(imageViewDrawer, animationDuration, isAppsDrawerFragmentVisible)
        animateImageViewTranslation(imageViewPhone, animationDuration, isAppsDrawerFragmentVisible)
        animateImageViewTranslation(imageViewContacts, animationDuration, isAppsDrawerFragmentVisible)
        animateImageViewTranslation(imageViewMessages, animationDuration, isAppsDrawerFragmentVisible)
        animateImageViewTranslation(imageViewBrowser, animationDuration, isAppsDrawerFragmentVisible)
        if (isAppsDrawerFragmentVisible) {
            gridDock.animate().alpha(0f).setDuration(500L).start()
        val isAppsDrawerVisible = currentFragment is AppsDrawer
        animateDock(500L, isAppsDrawerVisible)
        if (isAppsDrawerVisible) {
            dockRecycler.animate().alpha(0f).setDuration(500L).start()
        } else {
            dockRecycler.animate().alpha(1.0f).setDuration(500L).start()
        }
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

    // ---- Dock animation (replaces per-ImageView animation) ----

    private fun animateDock(duration: Long, slideDown: Boolean) {
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        dockRecycler.visibility = View.VISIBLE
        if (slideDown) {
            dockRecycler.translationY = 0f
            dockRecycler.animate().translationY(screenHeight).setDuration(duration).start()
        } else {
            dockRecycler.translationY = screenHeight
            dockRecycler.animate().translationY(0f).setDuration(duration).start()
        }
    }

    // ---- Theme helper ----

    private fun applyDockTheme(theme: String?) {
        val bg = when (theme) {
            "Mochi" -> AppCompatResources.getDrawable(requireContext(), R.color.mochilight)
            "Modern" -> AppCompatResources.getDrawable(requireContext(), R.drawable.modern_dock)
            "System" -> AppCompatResources.getDrawable(requireContext(), R.color.abt)
            else    -> AppCompatResources.getDrawable(requireContext(), R.drawable.quicklaunchbg)
        }
        dockRecycler.background = bg
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
            if (appWidgetInfos[j].provider.packageName == packageName && appWidgetInfos[j].provider.className == className) {
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
                recents.layoutManager = LinearLayoutManager(context)
                recents.itemAnimator = DefaultItemAnimator()
                val usm = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                // Query window expanded to match the 10-minute filter below
                val aslist = usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    time - 600000, time
                ).toMutableList()

                appStatsList = aslist.sortedBy { it.lastTimeUsed }.reversed() as MutableList<UsageStats>
                appStatsList.forEach { asl ->
                    if (asl.lastTimeUsed > time - 600000) {
                        apps?.forEach { app ->
                            if (app.packageName == asl.packageName) {
                                if (!usm.isAppInactive(asl.packageName)
                                    && asl.packageName != "com.achunt.weboslauncher"
                                    && asl.packageName != "com.achunt.justtype"
                                    && !goodbyeList.contains(asl.packageName)) {
                                    recentsList.add(app)
                                    Log.d("Recents", app.packageName)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val endFind = System.currentTimeMillis()
        Log.d("Recents Finder", "to call Recents " + (endFind - start))
    }


    private fun animateImageViewTranslation(
        imageView: ImageView,
        animationDuration: Long,
        down: Boolean
    ) {
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        imageView.visibility = View.VISIBLE
        if (down) {
            imageView.translationY = 0F
            imageView.animate()
                .translationY(screenHeight)
                .setDuration(animationDuration)
                .start()
        } else {
            imageView.translationY = screenHeight
            imageView.animate()
                .translationY(0f)
                .setDuration(animationDuration)
                .start()
        }
    }
        Log.d("Recents Finder", "to call Recents " + (System.currentTimeMillis() - start))
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
                    recentsList[pos].packageName as String
                )
            )
        }
    }

    class RecentsLongClickListener : View.OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            val recyclerView = v.rootView.findViewById<RecyclerView>(R.id.recents)
            val selectedItemPosition = recyclerView.getChildAdapterPosition(v)
            if (selectedItemPosition == RecyclerView.NO_POSITION) return
            val packageName = recentsList[selectedItemPosition].packageName
            val manager =
                v.rootView.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.killBackgroundProcesses(packageName as String?)
            goodbyeList.add(packageName as String)
            // Handler with explicit Looper — replaces deprecated no-arg Handler()
            Handler(Looper.getMainLooper()).postDelayed({
                recentsList.removeAt(selectedItemPosition)
                recentsAdapter.notifyItemRemoved(selectedItemPosition)
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
    }
}
