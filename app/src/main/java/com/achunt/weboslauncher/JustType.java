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

import java.util.Arrays;
import java.util.Objects;


public class JustType extends Fragment implements TextWatcher {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    EditText jt;
    TextView wbjt;
    RecyclerView.Adapter jtAdapter;
    volatile String contactName = "";
    volatile String contactNumber = "";
    String numberTest = "";
    volatile String[] cNameTest;
    volatile String[] cNumberTest;
    volatile String[] cName;
    volatile String[] cNumber;
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
                        if (wbjt.getText().toString().toLowerCase().startsWith(contactName)) {
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
        cNameTest = new String[phones.getCount()];
        cNumberTest = new String[phones.getCount()];
        int i = 0;
        while (phones.moveToNext()) {
            contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) >> 0);
            contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) >> 0);
            if (i > 0) {
                if (!cNameTest[i - 1].contains(contactName)) {
                    cNameTest[i] = contactName;
                    cNumberTest[i] = contactNumber;
                    i++;
                }
            } else {
                cNameTest[i] = contactName;
                cNumberTest[i] = contactNumber;
                i++;
            }
        }
        cName = Arrays.stream(cNameTest).filter(Objects::nonNull).toArray(String[]::new);
        cNumber = Arrays.stream(cNumberTest).filter(Objects::nonNull).toArray(String[]::new);
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
        String cont = "";
        for (int t = 1; t < split2.length; t++) {
            m3 += split2[t] + " ";
        }
        m3.trim();

        for (int t = 0; t < cName.length; t++) {
            cont = m2.substring(0, 1).toUpperCase() + m2.substring(1);
            if (cName[t].startsWith(cont)) {
                contactName = cName[t];
                contactNumber = cNumber[t];
            }
        }

        if (contactName.startsWith(cont)) {
            if (m2.length() > 0) {
                toSend = m3;
                toSend.toUpperCase();
            }
        }

        if (m1.length() > 3) {
            if (Character.isDigit(m2.charAt(0))) {
                m3 = "";
                String[] separate = m2.split(" ");
                contactNumber = separate[0];
                for (int t = 1; t < split2.length; t++) {
                    m3 += split2[t] + " ";
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
        final String[] m1 = {""};
        if (text.contains("Search the web for ")) {
            String[] t = temp.split("Search the web for ");
            temp = t[1];
            wbjt.setText(temp);
        }
        String finalTemp = temp;
        wbjt.setOnClickListener(v -> {
            String[] split1 = finalTemp.split("call ");
            if (split1.length > 1) {
                m1[0] = split1[1];
                for (int c = 0; c < cName.length; c++) {
                    String cont = m1[0].substring(0, 1).toUpperCase() + m1[0].substring(1);
                    if (cName[c].startsWith(cont)) {
                        contactName = cName[c];
                        contactNumber = cNumber[c];

                    }
                }
                if (m1[0].length() > 3) {
                    if (Character.isDigit(m1[0].charAt(0))) {
                        contactNumber = m1[0];
                    }
                }
            }
            intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contactNumber));
            startActivity(intent);
        });
    }
}
