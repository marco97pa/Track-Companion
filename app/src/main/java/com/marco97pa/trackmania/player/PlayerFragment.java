package com.marco97pa.trackmania.player;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.marco97pa.trackmania.BuildConfig;
import com.marco97pa.trackmania.MainActivity;
import com.marco97pa.trackmania.R;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PlayerFragment extends Fragment {

    private static final String LOG_TAG = "PlayerFragment";
    private String cookie;
    private TextView nicknameText;
    private ImageView imageView;
    private TextView APIverText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_player, container, false);

        nicknameText = root.findViewById(R.id.name);
        imageView = root.findViewById(R.id.image);

        TextView appVer = root.findViewById(R.id.app_ver);
        appVer.setText(BuildConfig.VERSION_NAME);
        APIverText = root.findViewById(R.id.api_ver);

        LinearLayout bug = root.findViewById(R.id.report_bug);
        bug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","marco97pa@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "[marco97pa@gmail.com]");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "I had a problem with " +
                        getString(R.string.app_name) + ", version " +  BuildConfig.VERSION_NAME +
                        ", API version " + APIverText.getText().toString());
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = root.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        cookie = ((MainActivity)getActivity()).getCookie();
        if(cookie == null){
            ((MainActivity) getActivity()).requestLogin();
        }

        new RetrievePlayerTask().execute(cookie);

    }

    public class RetrievePlayerTask extends AsyncTask<String, Void, Player> {

        private static final String LOG_TAG = "RetrievePlayerTask";

        protected Player doInBackground(String... cookie) {
            Log.d(LOG_TAG, "Starting task...");
            Log.d(LOG_TAG, "Cookie: " + cookie[0]);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("Cookie", cookie[0])
                    .url("https://players.trackmania.com/player")
                    .build();

            try (Response response = client.newCall(request).execute()) {

                Document doc = Jsoup.parse(response.body().string());

                Log.d(LOG_TAG, doc.title());
                String API = doc.select("footer nav .container span.navbar-text").text().substring(8);
                Log.d(LOG_TAG, "API " + API);

                String nickname = doc.select("#username").text();
                Log.d(LOG_TAG, nickname);
                String profile_pic = doc.select("#avatar").attr("src");
                Log.d(LOG_TAG, profile_pic);

                Player player = new Player(nickname, profile_pic, API);
                return player;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(Player player) {
            super.onPostExecute(player);

            //this method will be running on UI thread
            if(player != null) {
                if(player.getNickname() != null) {
                    nicknameText.setText(player.getNickname());
                    Picasso.get()
                            .load(player.getImage())
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(imageView);
                    APIverText.setText(player.getApi_version());
                }
                else{
                    ((MainActivity) getActivity()).requestLogin();
                }
            }
            else{
                Snackbar.make(imageView,getString(R.string.no_network), BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }

    }


}