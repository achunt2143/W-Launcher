package com.achunt.weboslauncher;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeScreen extends Fragment {

    ImageView imageViewDrawer;
    ImageView imageViewPhone;
    ImageView imageViewContacts;
    ImageView imageViewMessages;
    ImageView imageViewBrowser;

    public HomeScreen() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homescreen,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewDrawer = view.findViewById(R.id.icon_drawer);
        imageViewDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new AppsDrawer());
            }
        });
        imageViewPhone = view.findViewById(R.id.phone);
        imageViewPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phone = new Intent(Intent.ACTION_DIAL);
                startActivity(phone);
            }
        });
        imageViewContacts = view.findViewById(R.id.cnt);
        imageViewContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contacts = new Intent(Intent.ACTION_VIEW);
                contacts.setType(ContactsContract.Contacts.CONTENT_TYPE);
                startActivity(contacts);
            }
        });
        imageViewMessages = view.findViewById(R.id.msg);
        imageViewMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent msg = new Intent(Intent.ACTION_MAIN);
                msg.addCategory(Intent.CATEGORY_APP_MESSAGING);
                startActivity(msg);

            }
        });
        imageViewBrowser = view.findViewById(R.id.brs);
        imageViewBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browser = new Intent(Intent.ACTION_MAIN);
                browser.addCategory(Intent.CATEGORY_APP_BROWSER);
                startActivity(browser);

            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            fragment.setExitTransition(new Slide(Gravity.BOTTOM));
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
