package com.marco97pa.trackmania.player;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.marco97pa.trackmania.BuildConfig;
import com.marco97pa.trackmania.MainActivity;
import com.marco97pa.trackmania.R;
import com.marco97pa.trackmania.utils.FLog;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PlayerFragment extends Fragment {

    private static final String LOG_TAG = "PlayerFragment";
    private FLog log = new FLog(LOG_TAG);
    private String cookie, API;
    private TextView nicknameText;
    private ImageView imageView;
    private TextView APIverText;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    int taps = 0;

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

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            log.d( "Config params updated: " + updated);
                            checkSupportedApi();
                        } else {
                            log.d( "Config fetch failed");
                        }
                    }
                });

        //Sets version name easter egg
        LinearLayout APPver = (LinearLayout) root.findViewById(R.id.app_ver_layout);
        APPver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        if (taps == 7) {
                            Toast.makeText(getActivity(), getString(R.string.easter_egg), Toast.LENGTH_LONG).show();
                            watchYoutubeVideo(getActivity(), "X11cciTgwiM");
                            taps = 0;
                        }

                        taps++;
                    }
                });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        cookie = ((MainActivity)getActivity()).getCookie();
        if(cookie == null || cookie.isEmpty()){
            ((MainActivity) getActivity()).requestLogin();
            cookie = ((MainActivity)getActivity()).getCookie();
        }

        new RetrievePlayerTask().execute(cookie);

    }

    public class RetrievePlayerTask extends AsyncTask<String, Void, Player> {

        private static final String LOG_TAG = "RetrievePlayerTask";

        protected Player doInBackground(String... cookie) {
            log.d( "Starting task...");
            log.d( "Cookie: " + cookie[0]);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("Cookie", cookie[0])
                    .url("https://players.trackmania.com/player")
                    .build();

            try (Response response = client.newCall(request).execute()) {

                Document doc = Jsoup.parse(response.body().string());

                log.d( doc.title());
                String API = doc.select("footer nav .container span.navbar-text").text().substring(8);
                log.d( "API " + API);

                String nickname = doc.select("#username").text();
                log.d( nickname);
                String profile_pic = doc.select("#avatar").attr("src");
                log.d( profile_pic);

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
                if(player.getNickname() != null && player.getImage() != null && !player.getImage().isEmpty()) {

                    nicknameText.setText(player.getNickname());

                    Picasso.get()
                            .load(player.getImage())
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(imageView);

                    APIverText.setText(player.getApi_version());

                    checkSupportedApi();

                }
                else{
                    log.d( "Cookie is empty, authenticating again...");
                    ((MainActivity) getActivity()).requestLogin();
                    cookie = ((MainActivity)getActivity()).getCookie();
                    new RetrievePlayerTask().execute(cookie); //reload data
                }
            }
            else{
                Snackbar.make(imageView,getString(R.string.no_network), BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }

    }

    private void checkSupportedApi(){
        String supported_api = mFirebaseRemoteConfig.getString("supported_api");
        if(APIverText.getText().toString() != "" && supported_api != "none" && supported_api != "") {
            log.d( "Supported API (from Firebase): " + supported_api);
            log.d( "Actual API (from Ubisoft): " + APIverText.getText().toString());
            if (APIverText.getText().toString().contains(supported_api)) {
                APIverText.setTextColor(ContextCompat.getColor(getActivity(), R.color.green));
                log.d( "API version supported");
            } else {
                Log.w(LOG_TAG, "API version unsupported");
            }
        }
    }

    private static void watchYoutubeVideo(Context context, String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }
}
