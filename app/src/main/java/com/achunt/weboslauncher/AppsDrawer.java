package com.achunt.weboslauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


public class AppsDrawer extends Fragment {

    /** Set by HomeScreenK.loadFragment() right before this fragment is added, so the Light
     * theme's frosted-glass background can be built from what was actually on screen. One-shot:
     * consumed (and nulled out) the first time it's read. */
    public static Bitmap pendingBackdrop;

    volatile TabLayout tabLayout;
    volatile View view;
    volatile LinearLayoutManager mLayoutManager;
    CoordinatorLayout appsBG;
    private GestureDetector gestureDetector;

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
        Context context = getActivity();
        appsBG = view.findViewById(R.id.appsBG);
        // Leaves the dock's own strip at the bottom of the shared container uncovered, so it
        // stays visible and tappable instead of the drawer's opaque grid painting over it.
        // Set here rather than in the XML: this fragment's root is inflated with a null parent
        // (see onCreateView), so a layout_marginBottom on the root would be silently dropped —
        // it only takes effect once applied after the view is actually attached to its parent.
        ViewGroup.LayoutParams appsBGParams = appsBG.getLayoutParams();
        if (appsBGParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) appsBGParams).bottomMargin =
                    context.getResources().getDimensionPixelSize(R.dimen.dock_height);
            appsBG.setLayoutParams(appsBGParams);
        }
        // Rounds only the bottom corners, so the drawer reads as a card sitting just above the
        // dock's own rounded-top tray instead of butting squarely against it. Top corners stay
        // square by placing the round-rect's top edge above the visible bounds (y = -radius) —
        // the rounding curve for the top corners then falls entirely outside [0, height] and
        // never renders, while the bottom corners (within bounds) round normally.
        int cornerRadius = context.getResources().getDimensionPixelSize(R.dimen.webos_card_corner_radius);
        appsBG.setClipToOutline(true);
        appsBG.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setRoundRect(0, -cornerRadius, v.getWidth(), v.getHeight(), cornerRadius);
            }
        });
// Initialize gesture detector for swipe down to close
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                // Calculate vertical distance and velocity
                float deltaY = e2.getY() - e1.getY();
                float deltaX = e2.getX() - e1.getX();

                // Check for downward swipe with sufficient distance and velocity
                if (Math.abs(deltaY) > Math.abs(deltaX) && deltaY > 100 && velocityY > 1000) {
                    // Close the fragment
                    requireActivity().getSupportFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        });

        // Set touch listener to detect gestures
        view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true; // Consume the touch event
        });
        assert context != null;
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String theme = sharedPref.getString("themeName", ThemePreference.LIGHT);
        boolean isDark = ThemePreference.isDark(theme);
        boolean soundOn = sharedPref.getBoolean("sound", true);
        if (soundOn) {
            MediaPlayer mp = MediaPlayer.create(view.getContext(), R.raw.opendrawer);
            mp.setOnCompletionListener(mp1 -> {
                mp1.reset();
                mp1.release();
            });
            mp.start();
        }
        if (isDark) {
            appsBG.setBackground(AppCompatResources.getDrawable(context, R.drawable.classic3_bg));
        } else {
            appsBG.setBackground(buildFrostedGlassBackground(context));
        }

        // Dark theme keeps the gunmetal header; Light theme gets its own blue-toned header
        // (same accent-blue gradient as the About & Settings buttons) instead of reusing
        // Dark's gray chrome on top of the blue wallpaper.
        tabLayout.setBackgroundResource(isDark ? R.drawable.webos_header_bg : R.drawable.webos_header_bg_light);
        w.setStatusBarColor(ContextCompat.getColor(context, R.color.empty));
    }

    /**
     * webOS "frosted glass" look for the Light drawer. Previously this just laid the blue
     * wallpaper over the drawer at 86% opacity, but since AppsDrawer is added on top of
     * HomeScreenK rather than replacing it, that 14% gap let the still-fully-visible
     * recents cards and dock ghost through underneath — jarring rather than translucent.
     * Instead, stack: the always-opaque blue wallpaper (guarantees no raw transparency),
     * a heavily blurred snapshot of what HomeScreenK looked like right before this fragment
     * opened (pendingBackdrop, already downscaled by HomeScreenK — upscaling it here is
     * what blurs it), and a blue tint on top so the blurred content still reads as part of
     * the same blue theme instead of a smeared photo.
     */
    private Drawable buildFrostedGlassBackground(Context context) {
        Drawable wallpaper = AppCompatResources.getDrawable(context, R.drawable.classic_bg);
        Drawable tint = new ColorDrawable(ContextCompat.getColor(context, R.color.webos_accent_blue_dark));
        tint.setAlpha(190);

        Bitmap backdrop = pendingBackdrop;
        pendingBackdrop = null;
        if (backdrop == null) {
            return new LayerDrawable(new Drawable[]{wallpaper, tint});
        }
        BitmapDrawable blurred = new BitmapDrawable(context.getResources(), backdrop);
        blurred.setFilterBitmap(true);
        blurred.setGravity(Gravity.FILL);
        blurred.setAlpha(90);
        return new LayerDrawable(new Drawable[]{wallpaper, blurred, tint});
    }

}



