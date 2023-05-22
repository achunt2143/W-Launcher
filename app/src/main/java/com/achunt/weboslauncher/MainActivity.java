package com.achunt.weboslauncher;

import android.Manifest;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.Settings;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private HomeScreenK homeScreenFragment;

    public static List<UsageStats> getUsageStatsList(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DATE, -1);
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        return usageStatsList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadFragment(new HomeScreenK());
        Window w = getWindow();
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.empty));
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            checkPermission(Manifest.permission.READ_CONTACTS, 1);
        }*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS)
                != PackageManager.PERMISSION_GRANTED) {
            if (getUsageStatsList(this).isEmpty()) {
                Toast.makeText(this, "Please allow Usage Access", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
            checkPermission(Manifest.permission.PACKAGE_USAGE_STATS, 2);
        }
    }

    public boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            fragment.setRetainInstance(true);
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            fragment.setExitTransition(new Slide(Gravity.BOTTOM));
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, "home")
                    .setReorderingAllowed(true)
                    .addToBackStack("main")
                    .commit();
            if (fragment instanceof HomeScreenK) {
                homeScreenFragment = (HomeScreenK) fragment;
            } else {
                homeScreenFragment = null;
            }
            return true;
        }
        return false;
    }

    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
        } else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressed();
            getFragmentManager().popBackStack();
            Window w = getWindow();
            w.setStatusBarColor(ContextCompat.getColor(this, R.color.empty));
            LinearLayout widgets = findViewById(R.id.widgets);
            widgets.animate().alpha(1).setDuration(1000).start();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            LinearLayout widgets = findViewById(R.id.widgets);
            widgets.animate().alpha(1).setDuration(1000).start();
        } catch (Exception ignored) {
        }
        Window w = getWindow();
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.empty));

        if (homeScreenFragment != null) {
            homeScreenFragment.recentsList(getApplicationContext());
        }
    }


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}