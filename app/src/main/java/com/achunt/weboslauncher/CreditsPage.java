package com.achunt.weboslauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

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

        Context context = requireContext();
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isDark = ThemePreference.isDark(sharedPref.getString("themeName", ThemePreference.LIGHT));
        view.findViewById(R.id.creditsRoot)
                .setBackgroundResource(isDark ? R.drawable.classic3_bg : R.drawable.classic_bg);
        view.findViewById(R.id.creditsHeader).setBackground(
                ThemePreference.getHeaderDrawable(context));
        w.setStatusBarColor(ContextCompat.getColor(context, R.color.empty));

        TextView okay = view.findViewById(R.id.credClose);
        TextView t2 = (TextView) view.findViewById(R.id.cText);
        int bottomInset = (getActivity() instanceof MainActivity) ? ((MainActivity) getActivity()).getCurrentBottomInset() : 0;
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), bottomInset);

        t2.setMovementMethod(LinkMovementMethod.getInstance());
        okay.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                Fragment myFragment = new HomeScreenK();
                myFragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                getParentFragmentManager().beginTransaction().replace(R.id.container, myFragment).commit();
            }
        });

    }

}
