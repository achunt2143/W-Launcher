package com.achunt.weboslauncher;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class PagerAdapter extends FragmentPagerAdapter {

    private String[] TAB_TITLES = {"System", "Downloads", "Settings"};
    private int NUM_TABS = 3;

    public PagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        if (HomeScreenK.Companion.isHasWorkApps()) {
            TAB_TITLES = new String[]{"System", "Downloads", "Settings", "Work"};
            NUM_TABS = 4;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentTab();
            case 1:
                return new FragmentTabDownloads();
            case 2:
                return new FragmentTabSettings();
            case 3:
                return new Fragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_TABS;
    }
}
