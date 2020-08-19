package com.marco97pa.trackmania.maps;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marco97pa.trackmania.R;
import com.marco97pa.trackmania.utils.FLog;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/*
    Activity that shows more information about the Map.
    It gets information passed from bundle
 */
public class MapDetailsActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_DOWNLOAD = 8000;
    private static final String LOG_TAG = "MapDetailsActivity";
    private FLog log = new FLog(LOG_TAG);
    private TextView textBronze;
    private TextView textSilver;
    private TextView textGold;
    private TextView textAuthor;
    private String title, url, image_url, id, uid;
    private long score_author, score_gold, score_silver, score_bronze;
    private long downloadID;
    private Context context;

    //TODO Build leaderboard
    //Python: https://github.com/jonese1234/Trackmania-2020-Leaderboard-Scraper/blob/master/util/leaderboard.py
    //Concept: https://github.com/The-Firexx/trackmania2020apidocumentation/blob/master/LiveServices.md#get-apitokenleaderboardgrouppersonal_bestmapmapidsurround11

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("map_name");
        url = bundle.getString("map_url");
        image_url = bundle.getString("map_image");
        id = bundle.getString("map_id");
        uid = bundle.getString("map_uid");
        score_author = Long.parseLong(bundle.getString("map_score_author"));
        score_gold = Long.parseLong(bundle.getString("map_score_gold"));
        score_silver = Long.parseLong(bundle.getString("map_score_silver"));
        score_bronze = Long.parseLong(bundle.getString("map_score_bronze"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // First, request permission (Android 6.0+ only)
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        // You can use the API that requires the permission.
                        beginDownload(url, title);
                    }
                    else {
                        // You can directly ask for the permission.
                        requestPermissions(
                                new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                STORAGE_PERMISSION_DOWNLOAD);
                    }
                }
                else {
                    beginDownload(url, title);
                }
            }
        });

        context = this;

        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        ImageView image = (ImageView) findViewById(R.id.header_img);
        textAuthor = (TextView) findViewById(R.id.author);
        textGold = (TextView) findViewById(R.id.gold);
        textSilver = (TextView) findViewById(R.id.silver);
        textBronze = (TextView) findViewById(R.id.bronze);

        toolbarLayout.setTitle(title);
        if(!image_url.isEmpty()) {
            Picasso.get().load(image_url).into(image);
        }

        textAuthor.setText(convertTime(score_author));
        textGold.setText(convertTime(score_gold));
        textSilver.setText(convertTime(score_silver));
        textBronze.setText(convertTime(score_bronze));

    }

    public String convertTime(long millis){
        long mm = TimeUnit.MILLISECONDS.toMinutes(millis);
        long minutesMillis = TimeUnit.MINUTES.toMillis(mm);
        millis -= minutesMillis;
        long ss = TimeUnit.MILLISECONDS.toSeconds(millis);
        long secondsMillis = TimeUnit.SECONDS.toMillis(ss);
        millis -= secondsMillis;

        String stringInterval = "%02d:%02d.%03d";
        return String.format(stringInterval , mm, ss, millis);
    }


    private void beginDownload(String url, String title){

        //Create a DownloadManager.Request with all the information necessary to start the download
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle(title)// Title of the Download Notification
                .setDescription(getString(R.string.download_progress))// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".gbx")// Uri of the destination file
                .setAllowedOverMetered(true);// Set if download is allowed on Mobile network

        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
        Snackbar.make(textBronze, getString(R.string.download_progress), BaseTransientBottomBar.LENGTH_LONG).show();
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Snackbar.make(textBronze, getString(R.string.download_complete), BaseTransientBottomBar.LENGTH_LONG).show();
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(onDownloadComplete);
            log.d( "onDownloadComplete receiver unregistered");
        }catch (Exception e){
            log.d( "onDownloadComplete receiver is not registered");
        }
    }

    //ASK PERMISSION Android 6.0+ (Marshmallow)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_DOWNLOAD: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log.d( "Permission granted");
                    // permission was granted, yay!
                    beginDownload(url, title);

                } else {

                    // permission denied, boo!
                    Toast.makeText(this, getString(R.string.marshmallow_alert), Toast.LENGTH_LONG).show();

                }
                return;
            }
        }
    }

}
