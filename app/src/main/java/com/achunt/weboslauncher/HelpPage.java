package com.achunt.weboslauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Combined About + Settings page (formerly two separate screens — About with a gear icon
 * that drilled into a second Settings fragment). Every control here applies immediately,
 * webOS Mojo/Enyo-style — there's no longer a batched "OKAY" save step.
 */
public class HelpPage extends Fragment {

    public HelpPage() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_page, container, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window w = getActivity().getWindow();

        Context context = requireContext();
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        View root = view.findViewById(R.id.helpRoot);

        view.findViewById(R.id.rowGithub).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.github.com/achunt2143/W-Launcher/"))));

        view.findViewById(R.id.rowCredits).setOnClickListener(v -> {
            Fragment myFragment = new CreditsPage();
            myFragment.setExitTransition(new Slide(Gravity.TOP));
            getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> {
            Fragment myFragment = new HomeScreenK();
            myFragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
        });

        // ---- Theme: tapping a row applies it immediately, live-previewed on this page's
        // own background, instead of requiring a separate save step. ----
        boolean isDark = ThemePreference.isDark(sharedPref.getString("themeName", ThemePreference.LIGHT));
        applyTheme(view, root, w, isDark);

        view.findViewById(R.id.rowThemeLight).setOnClickListener(v -> {
            sharedPref.edit().putString("themeName", ThemePreference.LIGHT).apply();
            applyTheme(view, root, w, false);
        });
        view.findViewById(R.id.rowThemeDark).setOnClickListener(v -> {
            sharedPref.edit().putString("themeName", ThemePreference.DARK).apply();
            applyTheme(view, root, w, true);
        });

        // ---- Preferences: each switch persists on change, no OKAY button needed. ----
        SwitchMaterial soundSwitch = view.findViewById(R.id.switchSound);
        SwitchMaterial recentsSwitch = view.findViewById(R.id.switchRecents);
        SwitchMaterial notificationsSwitch = view.findViewById(R.id.switchNotifications);

        soundSwitch.setChecked(sharedPref.getBoolean("sound", true));
        recentsSwitch.setChecked(sharedPref.getBoolean("recents", false));
        notificationsSwitch.setChecked(sharedPref.getBoolean("notifications", false));

        soundSwitch.setOnCheckedChangeListener((btn, checked) -> {
            sharedPref.edit().putBoolean("sound", checked).apply();
            if (checked) {
                MediaPlayer mp = MediaPlayer.create(context, R.raw.tap_to_share);
                if (mp != null) {
                    mp.setOnCompletionListener(p -> {
                        p.reset();
                        p.release();
                    });
                    mp.start();
                }
            }
        });
        recentsSwitch.setOnCheckedChangeListener((btn, checked) ->
                sharedPref.edit().putBoolean("recents", checked).apply());
        notificationsSwitch.setOnCheckedChangeListener((btn, checked) ->
                sharedPref.edit().putBoolean("notifications", checked).apply());
    }

    /**
     * Applies [isDark] to everything on this page that depends on it: the page background,
     * the header bar (gunmetal for Dark, blue for Light — matching the apps drawer), the
     * status bar tint, and which row shows a checkmark. The about blurb and section labels
     * don't need a per-theme color anymore — they sit on a fixed dark scrim over the
     * wallpaper (see the ScrollView's background in the layout) that's dark enough in both
     * themes for light text to stay readable.
     */
    private void applyTheme(View view, View root, Window window, boolean isDark) {
        root.setBackgroundResource(isDark ? R.drawable.classic3_bg : R.drawable.classic_bg);
        view.findViewById(R.id.helpHeader).setBackgroundResource(
                isDark ? R.drawable.webos_header_bg : R.drawable.webos_header_bg_light);
        window.setStatusBarColor(ContextCompat.getColor(
                view.getContext(), isDark ? R.color.webos_header_start : R.color.abt));
        view.findViewById(R.id.checkLight).setVisibility(isDark ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.checkDark).setVisibility(isDark ? View.VISIBLE : View.GONE);
    }
}
