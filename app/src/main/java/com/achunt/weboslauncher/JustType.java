package com.achunt.weboslauncher;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class JustType extends Fragment implements TextWatcher {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    EditText jt;
    TextView wbjt;
    RecyclerView.Adapter jtAdapter;
    String contactName = "";
    String contactNumber = "";
    String numberTest = "";
    String[] cName;
    String[] cNumber;
    Intent intent;

    public JustType() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.justtype_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.justtype_view);
        layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);
        jt = view.findViewById(R.id.jtInput);

        Window w = getActivity().getWindow();
        w.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.status));

        new Thread(this::contactsSearch).start();

        jt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                jtAdapter = new JTAdapter(requireContext(), jt.getText().toString());
                recyclerView.setAdapter(jtAdapter);
                wbjt = view.findViewById(R.id.webText);
                wbjt.setText("Search the web for " + jt.getText().toString());

                wbjt.setOnClickListener(v -> webSearch());

                numberTest = wbjt.getText().toString();

                if (jt.getText().toString().toLowerCase().contains("send a text to")) {
                    if (wbjt.getText().toString().toLowerCase().contains("search the web for ")) {
                        String temp = wbjt.getText().toString();
                        String[] t = temp.split("Search the web for ");
                        temp = t[1];
                        wbjt.setText(temp);
                    }

                    wbjt.setOnClickListener(v -> {
                        if (wbjt.getText().toString().toLowerCase().contains(contactName)) {
                            sendText(wbjt.getText().toString());
                        } else if (Character.isDigit(numberTest.charAt(0))) {
                            sendText(wbjt.getText().toString());
                        }
                    });
                }

                if (jt.getText().toString().toLowerCase().contains("search youtube for")) {
                    searchYoutube(wbjt.getText().toString());
                }

                if (jt.getText().toString().toLowerCase().contains("search maps for")) {
                    searchMaps(wbjt.getText().toString());
                }

                if (jt.getText().toString().toLowerCase().startsWith("call")) {
                    doCall(wbjt.getText().toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public void contactsSearch() {
        Cursor phones = this.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        cName = new String[phones.getCount()];
        cNumber = new String[phones.getCount()];
        while (phones.moveToNext()) {
            int i = 0;
            contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) >> 0);
            cName[i] = contactName;
            contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) >> 0);
            cNumber[i] = contactNumber;
            i++;
        }
        contactNumber = "";
        contactName = "";
        phones.close();
    }

    public void webSearch() {
        intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, jt.getText().toString());
        startActivity(intent);
    }

    public void sendText(String text) {
        String message = text;
        String toSend = "";
        String[] split1 = message.split("send a text to ");
        String m1 = split1[1];
        String[] split2 = m1.split(" ");
        String m2 = split2[0];
        String m3 = "";
        for (int i = 1; i < split2.length; i++) {
            m3 += split2[i] + " ";
        }
        m3.trim();

        for (int i = 0; i < cName.length; i++) {
            if (cName[i].equalsIgnoreCase(m2)) {
                contactName = cName[i];
                contactNumber = cNumber[i];
                System.out.println("Contact " + contactName + " " + contactNumber);
            }
        }

        if (m2.equalsIgnoreCase(contactName)) {
            if (m2.length() > 0) {
                toSend = m3;
                toSend.toUpperCase();
                System.out.println("toSend " + toSend);
            }
        }

        if (m1.length() > 3) {
            if (Character.isDigit(m2.charAt(0))) {
                m3 = "";
                String[] separate = m2.split(" ");
                contactNumber = separate[0];
                for (int i = 1; i < split2.length; i++) {
                    m3 += split2[i] + " ";
                }
                toSend = m3;
            }
        }

        Uri uri = Uri.parse("smsto:" + contactNumber);
        intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", toSend);
        startActivity(intent);

    }

    public void searchYoutube(String text) {
        String temp = text;
        String m1 = "";
        if (text.contains("Search the web for ")) {
            String[] t = temp.split("Search the web for ");
            temp = t[1];
            wbjt.setText(temp);
        }
        String[] split1 = temp.split("search youtube for ");
        if (split1.length > 1) {
            m1 = split1[1];
        }
        String finalM = m1;
        wbjt.setOnClickListener(v -> {
            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + finalM));
                startActivity(intent);
            } catch (Exception e) {
                webSearch();
            }
        });
    }

    public void searchMaps(String text) {
        String temp = text;
        String m1 = "";
        if (text.contains("Search the web for ")) {
            String[] t = temp.split("Search the web for ");
            temp = t[1];
            wbjt.setText(temp);
        }
        String[] split1 = temp.split("search maps for ");
        if (split1.length > 1) {
            m1 = split1[1];
        }
        Uri geo = Uri.parse("geo:0,0?q=" + m1);
        wbjt.setOnClickListener(v -> {
            try {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geo);
                startActivity(intent);
            } catch (Exception e) {
                webSearch();
            }
        });
    }

    public void doCall(String text) {
        String temp = text;
        String m1 = "";
        if (text.contains("Search the web for ")) {
            String[] t = temp.split("Search the web for ");
            temp = t[1];
            wbjt.setText(temp);
        }
        String[] split1 = temp.split("call ");
        if (split1.length > 1) {
            m1 = split1[1];
            for (int i = 0; i < cName.length; i++) {
                if (cName[i].equalsIgnoreCase(m1)) {
                    contactName = cName[i];
                    contactNumber = cNumber[i];
                }
            }
            if (m1.length() > 3) {
                if (Character.isDigit(m1.charAt(0))) {
                    contactNumber = m1;
                }
            }
        }
        wbjt.setOnClickListener(v -> {
            intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contactNumber));
            startActivity(intent);
        });
    }
}
