package com.achunt.weboslauncher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


public class HomeScreen extends Fragment {

    ImageView imageViewDrawer;
    ImageView imageViewPhone;
    ImageView imageViewContacts;
    ImageView imageViewMessages;
    ImageView imageViewBrowser;
    ImageView imageJustType;
    static RecyclerView.Adapter adapter;

    public HomeScreen() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.homescreen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        adapter = new RAdapter(requireContext());
        Window w = getActivity().getWindow();
        w.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.empty));

        imageJustType = view.findViewById(R.id.justType);
        imageJustType.setOnClickListener(v -> {
            view.findViewById(R.id.justType).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.jtText).setVisibility(View.INVISIBLE);
            loadFragmentJT(new JustType());
        });

        imageViewDrawer = view.findViewById(R.id.icon_drawer);
        imageViewDrawer.setOnClickListener(v -> {

            loadFragment(new AppsDrawer());
            view.findViewById(R.id.justType).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.jtText).setVisibility(View.INVISIBLE);
        });

        imageViewPhone = view.findViewById(R.id.phone);
        imageViewPhone.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(RAdapter.appsList.get(RAdapter.phone).packageName.toString());
            context.startActivity(launchIntent);
        });

        imageViewContacts = view.findViewById(R.id.cnt);
        imageViewContacts.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(RAdapter.appsList.get(RAdapter.contacts).packageName.toString());
            context.startActivity(launchIntent);
        });

        imageViewMessages = view.findViewById(R.id.msg);
        imageViewMessages.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(RAdapter.appsList.get(RAdapter.messages).packageName.toString());
            context.startActivity(launchIntent);
        });

        imageViewBrowser = view.findViewById(R.id.brs);
        imageViewBrowser.setOnClickListener(v -> {
            Intent browser = new Intent(Intent.ACTION_MAIN);
            browser.addCategory(Intent.CATEGORY_APP_BROWSER);
            startActivity(browser);
        });

        view.findViewById(R.id.justType).setVisibility(View.VISIBLE);
        view.findViewById(R.id.jtText).setVisibility(View.VISIBLE);
    }

    public boolean loadFragment(Fragment fragment) {

        if (fragment != null) {
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            fragment.setExitTransition(new Slide(Gravity.BOTTOM));
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .setReorderingAllowed(true)
                    .commit();
            return true;
        }
        return false;
    }

    public boolean loadFragmentJT(Fragment fragment) {

        if (fragment != null) {
            fragment.setEnterTransition(new android.transition.AutoTransition());
            fragment.setExitTransition(new android.transition.AutoTransition());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .setReorderingAllowed(true)
                    .commit();
            return true;
        }
        return false;
    }
}
