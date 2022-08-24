package com.achunt.weboslauncher;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadFragment(new HomeScreen());
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            fragment.setExitTransition(new Slide(Gravity.BOTTOM));
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .setReorderingAllowed(true)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        loadFragment(new HomeScreen());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("*****************STARTING**************************");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("*****************RESUMING**************************");
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("*****************STOPPING**************************");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!this.isFinishing()) {
            System.out.println("*****************FINISHING**************************");
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        System.out.println("*****************FOCUS CHANGE**************************");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("*****************RESTART**************************");
        Intent restart = getPackageManager().getLaunchIntentForPackage("com.achunt.weboslauncher");
        startActivity(restart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("*****************DESTROY**************************");
    }

}