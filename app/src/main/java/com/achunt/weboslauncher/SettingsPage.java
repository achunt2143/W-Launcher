package com.achunt.weboslauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;


public class SettingsPage extends Fragment {

    public SettingsPage() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_page, container, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window w = getActivity().getWindow();
        w.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.abt));
        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        SwitchMaterial sound = view.findViewById(R.id.soundSwitch);
        SwitchMaterial recents = view.findViewById(R.id.recentsSwitch);
        sound.setChecked(sharedPref.getBoolean("sound", true));
        recents.setChecked(sharedPref.getBoolean("recents", false));
        ChipGroup theme = view.findViewById(R.id.chip_group);
        theme.check(sharedPref.getInt("themeChip", 1));
        Button okay = view.findViewById(R.id.setOkay);

        okay.setOnClickListener(v -> {
            Chip chip = view.findViewById(theme.getCheckedChipId());
            edit.putBoolean("sound", sound.isChecked());
            edit.putBoolean("recents", recents.isChecked());
            edit.putInt("themeChip", theme.getCheckedChipId());
            edit.putString("themeName", (String) chip.getText());
            edit.commit();
            Boolean soundOn = sharedPref.getBoolean("sound", true);
            if (soundOn) {
                MediaPlayer mp = MediaPlayer.create(view.getContext(), R.raw.tap_to_share);
                mp.setOnCompletionListener(mp1 -> {
                    mp1.reset();
                    mp1.release();
                });
                mp.start();
            }
            Fragment myFragment = new HomeScreenK();
            myFragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
        });

    }

}
