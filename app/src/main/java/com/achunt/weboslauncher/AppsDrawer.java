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

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
        view = inflater.inflate(R.layout.apps_drawer, container, false);
        tabLayout = view.findViewById(R.id.tabs);
        final ViewPager viewPager = view.findViewById(R.id.viewpager);
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window w = requireActivity().getWindow();
        Context context = getActivity();
        appsBG = view.findViewById(R.id.appsBG);
        updateBottomMargin();

        // Rounds all 4 corners (top and bottom) so the drawer reads as a floating webOS card sheet.
        int cornerRadius = context.getResources().getDimensionPixelSize(R.dimen.webos_card_corner_radius);
        appsBG.setClipToOutline(true);
        appsBG.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), cornerRadius);
            }
        });
        appsBG.setElevation(8 * context.getResources().getDisplayMetrics().density);
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
                    getParentFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        });

        // Set touch listener on header container to detect swipe-down-to-close gestures
        View headerContainer = view.findViewById(R.id.drawerHeaderContainer);
        if (headerContainer != null) {
            headerContainer.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        }

        // Just Type search listener
        EditText searchInput = view.findViewById(R.id.justTypeSearchInput);
        ImageView searchClear = view.findViewById(R.id.justTypeSearchClear);
        View searchContainer = view.findViewById(R.id.justTypeSearchContainer);

        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = (s != null) ? s.toString().trim() : "";
                    if (searchClear != null) {
                        searchClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    filterAllAdapters(query);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (searchClear != null && searchInput != null) {
            searchClear.setOnClickListener(v -> searchInput.setText(""));
        }

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

        if (headerContainer != null) {
            headerContainer.setBackground(ThemePreference.getHeaderDrawable(context));
        }
        if (searchContainer != null) {
            searchContainer.setBackground(ThemePreference.getSearchPillDrawable(context));
        }
        ImageView searchIcon = view.findViewById(R.id.justTypeSearchIcon);
        if (searchInput != null) {
            searchInput.setTextColor(ThemePreference.getSearchTextColor(context));
            searchInput.setHintTextColor(ThemePreference.getSearchHintColor(context));
        }
        if (searchIcon != null) {
            searchIcon.setColorFilter(ThemePreference.getSearchIconColor(context));
        }
        if (searchClear != null) {
            searchClear.setColorFilter(ThemePreference.getSearchIconColor(context));
        }
        if (tabLayout != null) {
            tabLayout.setSelectedTabIndicator(ThemePreference.getTabIndicator(context));
        }
        w.setStatusBarColor(ContextCompat.getColor(context, R.color.empty));
    }

    public void filterAllAdapters(String query) {
        filterAdapter(HomeScreenK.Companion.getAdapterSystem(), query);
        filterAdapter(HomeScreenK.Companion.getAdapterDownloads(), query);
        filterAdapter(HomeScreenK.Companion.getAdapterSettings(), query);
        filterAdapter(HomeScreenK.Companion.getAdapterWork(), query);
    }

    private void filterAdapter(Object adapter, String query) {
        if (adapter instanceof AppFilterable) {
            ((AppFilterable) adapter).filter(query);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        filterAllAdapters("");
    }

    /**
     * Recomputes and applies the bottom margin for appsBG so it rests cleanly above the dock
     * with an intentional visual gap (drawer_dock_gap), ensuring the card and dock do not touch.
     */
    public void updateBottomMargin() {
        if (appsBG == null || getContext() == null) return;
        int dockBottomPadding = 0;
        if (getActivity() instanceof MainActivity) {
            dockBottomPadding = ((MainActivity) getActivity()).getDockBottomPadding();
        }
        int baseDockHeight = getContext().getResources().getDimensionPixelSize(R.dimen.dock_height);
        int drawerDockGap = getContext().getResources().getDimensionPixelSize(R.dimen.drawer_dock_gap);
        int totalDockHeight = baseDockHeight + dockBottomPadding + drawerDockGap;

        ViewGroup.LayoutParams appsBGParams = appsBG.getLayoutParams();
        if (appsBGParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) appsBGParams).bottomMargin = totalDockHeight;
            appsBG.setLayoutParams(appsBGParams);
        }
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
        Drawable tint = new ColorDrawable(ThemePreference.getBackdropTintColor(context));

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



