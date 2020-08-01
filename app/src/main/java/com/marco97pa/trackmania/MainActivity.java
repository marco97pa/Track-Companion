package com.marco97pa.trackmania;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.marco97pa.trackmania.auth.Auth;
import com.marco97pa.trackmania.dashboard.Season;
import com.marco97pa.trackmania.utils.FLog;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    private FLog log = new FLog(LOG_TAG);
    private Auth auth;
    private Season season = null;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_more, R.id.navigation_dashboard, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        context = this;

        auth = new Auth(
            getIntent().getStringExtra("accessToken"),
            getIntent().getStringExtra("refreshToken")
        );

        /* SESSION COUNTDOWN
        * Each session expires 1 hour after login
        * This CountDownTimer shows a Dialog on screen if time is up and a new session must be done
        */

        new CountDownTimer(auth.getExpireTime(), 60000) {

            public void onTick(long millisUntilFinished) {
                log.d("Session token is still active, " + String.format("%d min",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)) + " remaining");
            }

            public void onFinish() {
                log.d("Session expired, we need to relogin");
                //Time is up, we need to relogin: show a dialog
                new MaterialAlertDialogBuilder(context)
                        .setTitle(getString(R.string.session_expired))
                        .setMessage(getString(R.string.relogin_needed))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }

        }.start();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        Intent intent= new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        System.exit(0);
    }

    public Auth getAuth() {
        return auth;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }
}
