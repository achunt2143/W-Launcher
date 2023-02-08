package com.achunt.weboslauncher;

import android.annotation.SuppressLint;
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


public class CreditsPage extends Fragment {

    public CreditsPage() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_credits_page, container, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window w = getActivity().getWindow();
        w.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.abt));
        Button okay = view.findViewById(R.id.credClose);
        okay.setOnClickListener(v -> {
            Fragment myFragment = new HomeScreen();
            myFragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
        });

    }

}
