package com.achunt.weboslauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


public class AppsDrawer extends Fragment {

    volatile TabLayout tabLayout;
    volatile View view;
    volatile LinearLayoutManager mLayoutManager;
    CoordinatorLayout appsBG;

    public AppsDrawer() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.apps_drawer, null);
        tabLayout = view.findViewById(R.id.tabs);
        final ViewPager viewPager = view.findViewById(R.id.viewpager);
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        tabLayout.addTab(tabLayout.newTab().setText("System"));
        tabLayout.addTab(tabLayout.newTab().setText("Downloads"));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window w = requireActivity().getWindow();
        w.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.status));
        Context context = getActivity();
        appsBG = view.findViewById(R.id.appsBG);

        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String theme = sharedPref.getString("themeName", "Classic");
        Boolean soundOn = sharedPref.getBoolean("sound", true);
        if (soundOn) {
            MediaPlayer mp = MediaPlayer.create(view.getContext(), R.raw.opendrawer);
            mp.setOnCompletionListener(mp1 -> {
                mp1.reset();
                mp1.release();
            });
            mp.start();
        }
        switch (theme) {
            case "Classic":  //classic
                appsBG.setBackground(context.getDrawable(R.drawable.bg));
                break;
            case "Mochi":  //mochi
                appsBG.setBackground(context.getDrawable(R.color.mochilight));
                break;
            case "Modern":  //modern
                appsBG.setBackground(context.getDrawable(R.color.mochigrey));
                break;
            case "System":  //system
                appsBG.setBackground(context.getDrawable(R.color.abt));
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        if (getParentFragmentManager().getBackStackEntryCount() > 1) {
            getParentFragmentManager().popBackStack();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}



