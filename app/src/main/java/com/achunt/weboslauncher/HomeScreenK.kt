package com.achunt.weboslauncher

import android.app.ActivityManager
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
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HomeScreenK : Fragment() {
    lateinit var imageViewDrawer: ImageView
    lateinit var imageViewPhone: ImageView
    lateinit var imageViewContacts: ImageView
    lateinit var imageViewMessages: ImageView
    lateinit var imageViewBrowser: ImageView
    lateinit var gridDock: GridLayout
    lateinit var widgets: LinearLayout
    lateinit var recents: RecyclerView
    lateinit var sharedPrefH: SharedPreferences
    lateinit var apps: List<AppInfo>
    lateinit var appsToPass: List<ResolveInfo>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.homescreen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageViewDrawer = view.findViewById(R.id.icon_drawer)
        imageViewPhone = view.findViewById(R.id.phone)
        imageViewContacts = view.findViewById(R.id.cnt)
        imageViewMessages = view.findViewById(R.id.msg)
        imageViewBrowser = view.findViewById(R.id.brs)
        gridDock = view.findViewById(R.id.dock)
        widgets = view.findViewById(R.id.widgets)
        recents = view.findViewById(R.id.recents)
        super.onViewCreated(view, savedInstanceState)
        adapter = RAdapter(requireContext())
        appsToPass = RAdapter(requireContext()).resolveList
        adapterSystem = RAdapterSystem(requireContext(), appsToPass)
        adapterDownloads = RAdapterDownloads(requireContext(), appsToPass)
        adapterSettings = RAdapterSettings(requireContext())
        apps = RAdapter(requireContext()).appsList

        val w = requireActivity().window
        w.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.empty)
        sharedPrefH = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val theme = sharedPrefH.getString("themeName", "Classic")
        val recents = sharedPrefH.getBoolean("recents", false)
        widgets.animate().alpha(1f).setDuration(1000).start()
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

        if (recents) {
            recentsList(requireContext())
        }

        when (theme) {
            "Classic" -> {
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
                gridDock.background = requireContext().getDrawable(R.color.mochilight)
            }
            "Modern" -> {
                imageViewPhone.setImageResource(R.drawable.modernphone)
                imageViewContacts.setImageResource(R.drawable.moderncontact)
                imageViewMessages.setImageResource(R.drawable.modernmessages)
                imageViewBrowser.setImageResource(R.drawable.modernbrowser)
                gridDock.background = requireContext().getDrawable(R.color.mochigrey)
            }
            "System" -> {
                val browserIntent = Intent("android.intent.action.VIEW", Uri.parse("http://"))
                val resolveInfo = view.context.packageManager.resolveActivity(
                    browserIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                imageViewPhone.setImageDrawable(RAdapter.appsList[RAdapter.phone].icon)
                imageViewContacts.setImageDrawable(RAdapter.appsList[RAdapter.contacts].icon)
                imageViewMessages.setImageDrawable(RAdapter.appsList[RAdapter.messages].icon)
                imageViewBrowser.setImageDrawable(
                    resolveInfo!!.activityInfo.applicationInfo.loadIcon(
                        requireContext().packageManager
                    )
                )
                gridDock.background = requireContext().getDrawable(R.color.abt)
            }
        }
        imageViewDrawer.setOnClickListener {
            widgets.animate().alpha(0f).setDuration(1000).start()
            loadFragment(AppsDrawer())
        }
        imageViewPhone.setOnClickListener { v: View ->
            val context = v.context
            if (RAdapter.phone > 0) {
                val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(RAdapter.appsList[RAdapter.phone].packageName.toString())
                context.startActivity(launchIntent)
            } else {
                val intent = Intent(Intent.ACTION_DIAL)
                context.startActivity(intent)
            }
        }
        imageViewContacts.setOnClickListener { v: View ->
            val context = v.context
            val launchIntent = context.packageManager
                .getLaunchIntentForPackage(RAdapter.appsList[RAdapter.contacts].packageName.toString())
            context.startActivity(launchIntent)
        }
        imageViewMessages.setOnClickListener { v: View ->
            val context = v.context
            val launchIntent = context.packageManager
                .getLaunchIntentForPackage(RAdapter.appsList[RAdapter.messages].packageName.toString())
            context.startActivity(launchIntent)
        }
        imageViewBrowser.setOnClickListener {
            val browser = Intent(Intent.ACTION_MAIN)
            browser.addCategory(Intent.CATEGORY_APP_BROWSER)
            startActivity(browser)
        }
    }

    fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            fragment.enterTransition = Slide(Gravity.BOTTOM)
            fragment.exitTransition = Slide(Gravity.BOTTOM)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(R.id.container, fragment, "apps")
                .addToBackStack("home")
                .setReorderingAllowed(true)
                .commit()
            return true
        }
        return false
    }

    fun createWidget(
        view: View,
        packageName: String,
        className: String,
        mAppWidgetHost: AppWidgetHost,
        mAppWidgetManager: AppWidgetManager
    ): Boolean {
        // Get the list of installed widgets
        var newAppWidgetProviderInfo: AppWidgetProviderInfo? = null
        val appWidgetInfos: List<AppWidgetProviderInfo>
        appWidgetInfos = mAppWidgetManager.installedProviders
        var widgetIsFound = false
        for (j in appWidgetInfos.indices) {
            if (appWidgetInfos[j].provider.packageName == packageName && appWidgetInfos[j].provider.className == className) {
                // Get the full info of the required widget
                newAppWidgetProviderInfo = appWidgetInfos[j]
                widgetIsFound = true
                break
            }
        }
        return if (!widgetIsFound) {
            false
        } else {
            // Create Widget
            val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
            val hostView =
                mAppWidgetHost.createView(view.context, appWidgetId, newAppWidgetProviderInfo)
            hostView.setAppWidget(appWidgetId, newAppWidgetProviderInfo)

            // Add it to your layout
            val widgetLayout = view.findViewById<LinearLayout>(R.id.widgets)
            widgetLayout.addView(hostView)

            // And bind widget IDs to make them actually work
            val allowed = mAppWidgetManager.bindAppWidgetIdIfAllowed(
                appWidgetId,
                newAppWidgetProviderInfo!!.provider
            )
            if (!allowed) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                intent.putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                    newAppWidgetProviderInfo.provider
                )
                val REQUEST_BIND_WIDGET = 200906
                startActivityForResult(intent, REQUEST_BIND_WIDGET)
            }
            true
        }
    }

    fun recentsList(context: Context) {
        val sharedPrefH1 = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (sharedPrefH1.getBoolean("recents", false)) {
            try {
                if (!recentsList.isEmpty()) {
                    recentsList = mutableListOf()
                }
                val layoutManager = LinearLayoutManager(context)
                recents.layoutManager = layoutManager
                recents.itemAnimator = DefaultItemAnimator()
                val usm = requireContext().getSystemService(
                    Context.USAGE_STATS_SERVICE
                ) as UsageStatsManager

                val time = System.currentTimeMillis()
                val aslist = usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    time - 10000, time
                ).toMutableList()

                appStatsList = aslist.sortedBy {
                    it.lastTimeUsed
                }.reversed() as MutableList<UsageStats>

                appStatsList.forEach { asl ->
                    if (asl.lastTimeUsed > time - 600000) {
                        apps.forEach { app ->
                            if (app.packageName.equals(asl.packageName)) {
                                if (!usm.isAppInactive(asl.packageName)) {
                                    if (!asl.packageName.equals("com.achunt.weboslauncher")) {
                                        if (!asl.packageName.equals("com.achunt.justtype")) {
                                            recentsList.add(app)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val bye = mutableListOf<Int>()
                for (i in recentsList) {
                    for (j in goodbyeList) {
                        if (i.packageName.equals(j.packageName)) {
                            bye.add(recentsList.indexOf(i))
                        }
                    }
                }
                for (i in bye) {
                    recentsList.removeAt(i)
                }
                val horizontalLayout = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                recents.layoutManager = horizontalLayout
                recentsAdapter = RecentsAdapter(recentsList)
                recents.adapter = recentsAdapter

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    class RecentsClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            launchItem(v)
        }
        private fun launchItem(v: View) {
            val recyclerView = v.rootView.findViewById<RecyclerView>(R.id.recents)
            val selectedItemPosition = recyclerView.getChildPosition(v)
            v.context.startActivity(
                v.context.packageManager.getLaunchIntentForPackage(
                    recentsList[selectedItemPosition].packageName as String
                )
            )
        }
    }

    class RecentsLongClickListener : View.OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            removeItem(v)
            return true
        }

        private fun removeItem(v: View) {
            v.animate()
                .translationY(-2000f)
                .setDuration(500)
                .start()
            val recyclerView = v.rootView.findViewById<RecyclerView>(R.id.recents)
            val selectedItemPosition = recyclerView.getChildAdapterPosition(v)
            val manager =
                v.rootView.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.killBackgroundProcesses(recentsList[selectedItemPosition].packageName as String?)
            Handler().postDelayed({
                recentsList.removeAt(selectedItemPosition)
                recentsAdapter.notifyItemRemoved(selectedItemPosition)
            }, 500)
        }
    }

    companion object {
        @Volatile
        lateinit var adapter: RecyclerView.Adapter<*>

        @Volatile
        lateinit var adapterSystem: RecyclerView.Adapter<*>

        @Volatile
        lateinit var adapterDownloads: RecyclerView.Adapter<*>

        @Volatile
        lateinit var adapterSettings: RecyclerView.Adapter<*>
        const val APPWIDGET_HOST_ID = 200906
        lateinit var appStatsList: MutableList<UsageStats>
        lateinit var recentsAdapter: RecentsAdapter
        var recentsList = mutableListOf<AppInfo>()
        var goodbyeList = mutableListOf<AppInfo>()
    }
}
