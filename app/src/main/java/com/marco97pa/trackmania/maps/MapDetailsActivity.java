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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marco97pa.trackmania.R;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MapDetailsActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_DOWNLOAD = 8000;
    private static final String LOG_TAG = "MapDetailsActivity";
    private TextView textBronze;
    private TextView textSilver;
    private TextView textGold;
    private TextView textAuthor;
    private Button downloadButton;
    private String download, title, url, image_url;
    private long downloadID;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("map_title");
        url = bundle.getString("map_url");
        image_url = bundle.getString("map_image");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = getIntent().getExtras();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
                // (Optional) Here we're setting the title of the content
                sendIntent.putExtra(Intent.EXTRA_TITLE, title);
                // Show the Sharesheet
                startActivity(Intent.createChooser(sendIntent, null));
            }
        });

        downloadButton = (Button) findViewById(R.id.button);
        downloadButton.setEnabled(false);
        context = this;
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // First, request permission (Android 6.0+ only)
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        // You can use the API that requires the permission.
                        beginDownload(download, title);
                    }
                    else {
                        // You can directly ask for the permission.
                        requestPermissions(
                                new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                STORAGE_PERMISSION_DOWNLOAD);
                    }
                }
                else {
                    beginDownload(download, title);
                }
            }
        });

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

        new DecodeGBXTask().execute(bundle.getString("cookie"), url);
    }

    public class DecodeGBXTask extends AsyncTask<String, Void, String> {

        private static final String LOG_TAG = "DecodeGBXTask";

        protected String doInBackground(String... params) {
            Log.d(LOG_TAG, "Starting task...");
            Log.d(LOG_TAG, "Cookie: " + params[0]);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("Cookie", params[0])
                    .url(params[1])
                    .build();

            try (Response response = client.newCall(request).execute()) {

                Document doc = Jsoup.parse(response.body().string());
                download = doc.select(".btn-primary").attr("href");
                Log.d(LOG_TAG, download);

                Request download_request = new Request.Builder().url(download).build();
                Response download_response = client.newCall(download_request).execute();

                InputStream is = download_response.body().byteStream();
                byte[] buffer = new byte[1000];
                is.read(buffer);
                is.close();

                String s = new String(buffer);

                Log.d(LOG_TAG, s);
                return s;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            downloadButton.setEnabled(true);

            long time_bronze = scrapeMedal(s,"bronze");
            String bronze = convertTime(time_bronze);
            textBronze.setText(bronze);

            long time_silver = scrapeMedal(s, "silver");
            String silver = convertTime(time_silver);
            textSilver.setText(silver);

            long time_gold = scrapeMedal(s, "gold");
            String gold = convertTime(time_gold);
            textGold.setText(gold);

            long time_author = scrapeMedal(s, "authortime");
            String authortime = convertTime(time_author);
            textAuthor.setText(authortime);
        }
    }

    public String convertTime(long millis){
        long mm = TimeUnit.MILLISECONDS.toMinutes(millis);
        long minutesMillis = TimeUnit.MINUTES.toMillis(mm);
        millis -= minutesMillis;
        long ss = TimeUnit.MILLISECONDS.toSeconds(millis);
        long secondsMillis = TimeUnit.SECONDS.toMillis(ss);
        millis -= secondsMillis;

        String stringInterval = "%02d:%02d.%03d";
        Log.d(DecodeGBXTask.LOG_TAG + " - Convert", String.format(stringInterval , mm, ss, millis));
        return String.format(stringInterval , mm, ss, millis);
    }

    public long scrapeMedal(String file, String medal){
        int start, end;
        long time;
        Log.d(DecodeGBXTask.LOG_TAG, "Scraping medal...");
        String search = medal.concat("=\"");
        start = file.indexOf(search) + search.length();
        end = file.indexOf("\"", start);
        time = Long.parseLong(file.substring(start, end));
        Log.d(DecodeGBXTask.LOG_TAG + " - " + medal, Long.toString(time));
        return time;
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
        Snackbar.make(downloadButton, getString(R.string.download_progress), BaseTransientBottomBar.LENGTH_LONG).show();
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Snackbar.make(downloadButton, getString(R.string.download_complete), BaseTransientBottomBar.LENGTH_LONG).show();
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(onDownloadComplete);
            Log.d(LOG_TAG, "onDownloadComplete receiver unregistered");
        }catch (Exception e){
            Log.d(LOG_TAG, "onDownloadComplete receiver is not registered");
        }
    }

    //ASK PERMISSION Android 6.0+ (Marshmallow)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_DOWNLOAD: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "Permission granted");
                    // permission was granted, yay!
                    beginDownload(download, title);

                } else {

                    // permission denied, boo!
                    Toast.makeText(this, getString(R.string.marshmallow_alert), Toast.LENGTH_LONG).show();

                }
                return;
            }
        }
    }
}
