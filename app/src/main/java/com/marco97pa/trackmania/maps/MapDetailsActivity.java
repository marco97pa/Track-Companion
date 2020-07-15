package com.marco97pa.trackmania.maps;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.marco97pa.trackmania.R;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MapDetailsActivity extends AppCompatActivity {

    private TextView textBronze;
    private TextView textSilver;
    private TextView textGold;
    private TextView textAuthor;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = getIntent().getExtras();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, bundle.getString("map_url"));
                sendIntent.setType("text/plain");
                // (Optional) Here we're setting the title of the content
                sendIntent.putExtra(Intent.EXTRA_TITLE, bundle.getString("map_title"));
                // Show the Sharesheet
                startActivity(Intent.createChooser(sendIntent, null));
            }
        });

        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        ImageView image = (ImageView) findViewById(R.id.header_img);
        textAuthor = (TextView) findViewById(R.id.author);
        textGold = (TextView) findViewById(R.id.gold);
        textSilver = (TextView) findViewById(R.id.silver);
        textBronze = (TextView) findViewById(R.id.bronze);

        toolbarLayout.setTitle(bundle.getString("map_title"));
        if(!bundle.getString("map_image").isEmpty()) {
            Picasso.get().load(bundle.getString("map_image")).into(image);
        }
        new DecodeGBXTask().execute(bundle.getString("cookie"), bundle.getString("map_url"));
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
                String download = doc.select(".btn-primary").attr("href");
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

}
