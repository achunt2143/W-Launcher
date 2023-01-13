package com.achunt.weboslauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class HomeScreen extends Fragment {

    ImageView imageViewDrawer;
    ImageView imageViewPhone;
    ImageView imageViewContacts;
    ImageView imageViewMessages;
    ImageView imageViewBrowser;
    volatile static RecyclerView.Adapter adapter;
    volatile static RecyclerView.Adapter adapterSystem;
    volatile static RecyclerView.Adapter adapterDownloads;
    volatile static RecyclerView.Adapter adapterSettings;
    static final int APPWIDGET_HOST_ID = 200906;


    public HomeScreen() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.homescreen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        adapter = new RAdapter(requireContext());
        adapterSystem = new RAdapterSystem(requireContext());
        adapterDownloads = new RAdapterDownloads(requireContext());
        adapterSettings = new RAdapterSettings(requireContext());
        Window w = requireActivity().getWindow();
        w.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.empty));

        AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(view.getContext());
        AppWidgetHost mAppWidgetHost = new AppWidgetHost(view.getContext(), APPWIDGET_HOST_ID);

        try {
            createWidget(view, "com.achunt.justtype", "com.achunt.justtype.JustTypeWidget", mAppWidgetHost, mAppWidgetManager);
        } catch (Exception e) {
            Log.d("JTError", e.toString());
        }

        imageViewDrawer = view.findViewById(R.id.icon_drawer);
        imageViewDrawer.setOnClickListener(v -> loadFragment(new AppsDrawer()));

        imageViewPhone = view.findViewById(R.id.phone);
        imageViewPhone.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(RAdapter.appsList.get(RAdapter.phone).packageName.toString());
            context.startActivity(launchIntent);
        });

        imageViewContacts = view.findViewById(R.id.cnt);
        imageViewContacts.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(RAdapter.appsList.get(RAdapter.contacts).packageName.toString());
            context.startActivity(launchIntent);
        });

        imageViewMessages = view.findViewById(R.id.msg);
        imageViewMessages.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(RAdapter.appsList.get(RAdapter.messages).packageName.toString());
            context.startActivity(launchIntent);
        });

        imageViewBrowser = view.findViewById(R.id.brs);
        imageViewBrowser.setOnClickListener(v -> {
            Intent browser = new Intent(Intent.ACTION_MAIN);
            browser.addCategory(Intent.CATEGORY_APP_BROWSER);
            startActivity(browser);
        });

    }

    public boolean loadFragment(Fragment fragment) {

        if (fragment != null) {
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            fragment.setExitTransition(new Slide(Gravity.BOTTOM));
            this.requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }

    public boolean createWidget(View view, String packageName, String className, AppWidgetHost mAppWidgetHost, AppWidgetManager mAppWidgetManager) {
        // Get the list of installed widgets
        AppWidgetProviderInfo newAppWidgetProviderInfo = null;
        List<AppWidgetProviderInfo> appWidgetInfos;
        appWidgetInfos = mAppWidgetManager.getInstalledProviders();
        boolean widgetIsFound = false;
        for (int j = 0; j < appWidgetInfos.size(); j++) {
            if (appWidgetInfos.get(j).provider.getPackageName().equals(packageName) && appWidgetInfos.get(j).provider.getClassName().equals(className)) {
                // Get the full info of the required widget
                newAppWidgetProviderInfo = appWidgetInfos.get(j);
                widgetIsFound = true;
                break;
            }
        }

        if (!widgetIsFound) {
            return false;
        } else {
            // Create Widget
            int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
            AppWidgetHostView hostView = mAppWidgetHost.createView(view.getContext(), appWidgetId, newAppWidgetProviderInfo);
            hostView.setAppWidget(appWidgetId, newAppWidgetProviderInfo);

            // Add it to your layout
            LinearLayout widgetLayout = view.findViewById(R.id.widgets);
            widgetLayout.addView(hostView);

            // And bind widget IDs to make them actually work
            boolean allowed = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, newAppWidgetProviderInfo.provider);

            if (!allowed) {
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, newAppWidgetProviderInfo.provider);
                final int REQUEST_BIND_WIDGET = 200906;
                startActivityForResult(intent, REQUEST_BIND_WIDGET);
            }

            return true;
        }
    }


}
