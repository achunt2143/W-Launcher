package com.achunt.weboslauncher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AppsDrawer extends Fragment {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    public AppsDrawer() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.apps_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.appDrawer);
        layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(HomeScreen.adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("*****************STARTING APPSDRAWER**************************");
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("*****************RESUMING APPSDRAWER**************************");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("*****************STOPPING APPSDRAWER**************************");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("*****************FINISHING APPSDRAWER**************************");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("*****************DESTROY APPSDRAWER**************************");
    }
}

