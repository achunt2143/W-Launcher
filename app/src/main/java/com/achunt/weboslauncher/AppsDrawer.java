package com.achunt.weboslauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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

        assert context != null;
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String theme = sharedPref.getString("themeName", "Classic");
        boolean soundOn = sharedPref.getBoolean("sound", true);
        if (soundOn) {
            MediaPlayer mp = MediaPlayer.create(view.getContext(), R.raw.opendrawer);
            mp.setOnCompletionListener(mp1 -> {
                mp1.reset();
                mp1.release();
            });
            mp.start();
        }
        switch (theme) {
            case "Classic" -> { //classic
                Drawable bg = AppCompatResources.getDrawable(context, R.drawable.classic_bg);
                assert bg != null;
                bg.setAlpha(220);
                appsBG.setBackground(bg);
                //appsBG.setAlpha(0.5F);
            }
            case "Classic3" ->  //classic3
                    appsBG.setBackground(AppCompatResources.getDrawable(context, R.drawable.classic3_bg));
            case "Mochi" ->  //mochi
                    appsBG.setBackground(AppCompatResources.getDrawable(context, R.color.mochiBG));
            case "Modern" ->  //modern
                    appsBG.setBackground(AppCompatResources.getDrawable(context, R.color.modernBG));
            case "System" -> { //system
                appsBG.setBackground(AppCompatResources.getDrawable(context, R.color.systemBG));
            }


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
            if (getParentFragmentManager().findFragmentByTag("apps") != null) {
                getParentFragmentManager().popBackStack("apps", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            super.onPause();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}



