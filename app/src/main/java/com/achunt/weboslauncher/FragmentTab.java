package com.achunt.weboslauncher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentTab extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.apps_drawer, container, false);

        recyclerView = view.findViewById(R.id.appDrawer);
        layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(HomeScreen.adapterSystem);
        super.onViewCreated(view, savedInstanceState);
        return view;
    }
}
