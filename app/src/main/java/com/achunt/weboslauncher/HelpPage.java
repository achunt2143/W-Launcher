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
import android.widget.Toast;

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
        int bottomInset = (getActivity() instanceof MainActivity) ? ((MainActivity) getActivity()).getCurrentBottomInset() : 0;
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), bottomInset);

        view.findViewById(R.id.rowGithub).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.github.com/achunt2143/W-Launcher/"))));

        view.findViewById(R.id.rowCredits).setOnClickListener(v -> {
            Fragment myFragment = new CreditsPage();
            myFragment.setExitTransition(new Slide(Gravity.TOP));
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, myFragment)
                    .addToBackStack("credits")
                    .commit();
        });

        view.findViewById(R.id.btnRestart).setOnClickListener(v -> {
            Toast.makeText(context, "Restarting...", Toast.LENGTH_SHORT).show();
            MainActivity.restart(context);
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                Fragment myFragment = new HomeScreenK();
                myFragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
            }
        });

        // ---- Theme: tapping a row applies it immediately, live-previewed on this page's
        // own background, instead of requiring a separate save step. ----
        String currentTheme = ThemePreference.getTheme(context);
        applyTheme(view, root, w, currentTheme);

        View rowMaterialYou = view.findViewById(R.id.rowThemeMaterialYou);
        if (rowMaterialYou != null) {
            rowMaterialYou.setOnClickListener(v -> {
                sharedPref.edit().putString("themeName", ThemePreference.MATERIAL_YOU).apply();
                applyTheme(view, root, w, ThemePreference.MATERIAL_YOU);
            });
        }
        view.findViewById(R.id.rowThemeLight).setOnClickListener(v -> {
            sharedPref.edit().putString("themeName", ThemePreference.LIGHT).apply();
            applyTheme(view, root, w, ThemePreference.LIGHT);
        });
        view.findViewById(R.id.rowThemeDark).setOnClickListener(v -> {
            sharedPref.edit().putString("themeName", ThemePreference.DARK).apply();
            applyTheme(view, root, w, ThemePreference.DARK);
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
     * Applies [themeName] to this page: the page background, the header bar (dynamic for
     * Material You, gunmetal for Dark, blue for Classic Light), and toggles checkmarks.
     */
    private void applyTheme(View view, View root, Window window, String themeName) {
        boolean isDark = ThemePreference.isDark(themeName);
        boolean isMY = ThemePreference.isMaterialYou(themeName);

        root.setBackgroundResource(isDark ? R.drawable.classic3_bg : R.drawable.classic_bg);
        view.findViewById(R.id.helpHeader).setBackground(ThemePreference.getHeaderDrawable(view.getContext()));
        window.setStatusBarColor(ContextCompat.getColor(
                view.getContext(), R.color.empty));

        View checkMY = view.findViewById(R.id.checkMaterialYou);
        if (checkMY != null) {
            checkMY.setVisibility(isMY ? View.VISIBLE : View.GONE);
        }
        view.findViewById(R.id.checkLight).setVisibility((!isDark && !isMY) ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.checkDark).setVisibility(isDark ? View.VISIBLE : View.GONE);
    }
}
