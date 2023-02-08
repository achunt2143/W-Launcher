package com.achunt.weboslauncher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class HelpPage extends Fragment {


    public HelpPage() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help_page, container, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window w = getActivity().getWindow();
        w.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.abt));
        ImageView github = view.findViewById(R.id.github);
        github.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.github.com/achunt2143/W-Launcher/"))));
        Button credits = view.findViewById(R.id.infoCredits);
        ImageView settings = view.findViewById(R.id.settingsIcon);
        Button close = view.findViewById(R.id.infoClose);
        settings.setOnClickListener(v -> {
            Fragment myFragment = new SettingsPage();
            myFragment.setExitTransition(new Slide(Gravity.TOP));
            getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
        });
        credits.setOnClickListener(v -> {
            Fragment myFragment = new CreditsPage();
            myFragment.setExitTransition(new Slide(Gravity.TOP));
            getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
        });
        close.setOnClickListener(v -> {
            Fragment myFragment = new HomeScreen();
            myFragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
        });
    }
}
