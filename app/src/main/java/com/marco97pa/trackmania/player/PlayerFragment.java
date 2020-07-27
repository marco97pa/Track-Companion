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
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marco97pa.trackmania.BuildConfig;
import com.marco97pa.trackmania.MainActivity;
import com.marco97pa.trackmania.R;
import com.marco97pa.trackmania.utils.FLog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

public class PlayerFragment extends Fragment {

    private static final String LOG_TAG = "PlayerFragment";
    private FLog log = new FLog(LOG_TAG);
    private String API;
    private Player player;
    private TextView nicknameText, zoneText;
    private TextView pointsText, t1Text, t2Text, t3Text, t4Text, t5Text, t6Text, t7Text, t8Text, t9Text;
    private TextView APIverText;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    int taps = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_player, container, false);

        nicknameText = root.findViewById(R.id.name);
        zoneText = root.findViewById(R.id.zone);

        TextView appVer = root.findViewById(R.id.app_ver);
        appVer.setText(BuildConfig.VERSION_NAME);

        APIverText = root.findViewById(R.id.api_ver);

        pointsText = root.findViewById(R.id.points);
        t1Text = root.findViewById(R.id.bronze1);
        t2Text = root.findViewById(R.id.bronze2);
        t3Text = root.findViewById(R.id.bronze3);
        t4Text = root.findViewById(R.id.silver1);
        t5Text = root.findViewById(R.id.silver2);
        t6Text = root.findViewById(R.id.silver3);
        t7Text = root.findViewById(R.id.gold1);
        t8Text = root.findViewById(R.id.gold2);
        t9Text = root.findViewById(R.id.gold3);

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
                        ", API version " + API);
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

        player = new Player(
                ((MainActivity)getActivity()).getAuth().getAccountId(),
                ((MainActivity)getActivity()).getAuth().getUsername()
        );

        API = ((MainActivity)getActivity()).getAuth().getAPIversion();
        APIverText.setText(API);
        nicknameText.setText(player.getUsername());
        new getPlayerZoneTask().execute(
                ((MainActivity)getActivity()).getAuth().getAccessToken(),
                player.getAccountID());
        new getPlayerTrophiesTask().execute(
                ((MainActivity)getActivity()).getAuth().getAccessToken(),
                player.getAccountID());
    }

    /*
     * GET PLAYER'S TROPHIES FROM ACCOUNTID
     * HTTP GET to https://prod.trackmania.core.nadeo.online/accounts/{accountID}/trophies/lastYearSummary
     * Headers:
     *   Authorization = nadeo_v1 t={token from Auth stage 1}
     *
     * See also https://www.trackmania.com/2020/06/26/the-rankings-system-in-trackmania/
     *
     */
    public class getPlayerTrophiesTask extends AsyncTask<String, Void, Trophy> {

        private static final String LOG_TAG = "getPlayerTrophiesTask";
        private FLog log = new FLog(LOG_TAG);

        private static final int RESPONSE_OK = 200;
        private Trophy trophy;

        protected Trophy doInBackground(String... params) {
            log.d("Starting task...");
            String token = params[0];
            String accountID = params[1];

            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url("https://prod.trackmania.core.nadeo.online/accounts/" + accountID + "/trophies/lastYearSummary")
                        .addHeader("Authorization", "nadeo_v1 t=" + token)
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.code() == RESPONSE_OK) {
                    String jsonData = response.body().string();
                    Gson gson = new Gson();
                    Type trophyType = new TypeToken<Trophy>() { }.getType();
                    trophy = gson.fromJson(jsonData, trophyType);
                    JSONObject Jobject = new JSONObject(jsonData);
                } else {
                    log.d("Response: " + response.code());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return trophy;
        }

        @Override
        protected void onPostExecute(Trophy trophy) {
            super.onPostExecute(trophy);
            //execution on main/UI thread
            pointsText.setText( Integer.toString(trophy.getPoints()) );
            t1Text.setText( Integer.toString(trophy.getT1()) );
            t2Text.setText( Integer.toString(trophy.getT2()) );
            t3Text.setText( Integer.toString(trophy.getT3()) );
            t4Text.setText( Integer.toString(trophy.getT4()) );
            t5Text.setText( Integer.toString(trophy.getT5()) );
            t6Text.setText( Integer.toString(trophy.getT6()) );
            t7Text.setText( Integer.toString(trophy.getT7()) );
            t8Text.setText( Integer.toString(trophy.getT8()) );
            t9Text.setText( Integer.toString(trophy.getT9()) );
        }
    }

    /*
     * GET PLAYER'S ZONE FROM ACCOUNTID
     * HTTP GET to https://prod.trackmania.core.nadeo.online/accounts/{accountID}/zone
     * Headers:
     *   Authorization = nadeo_v1 t={token from Auth stage 1}
     *
     */
    public class getPlayerZoneTask extends AsyncTask<String, Void, String> {

        private static final String LOG_TAG = "getPlayerZoneTask";
        private FLog log = new FLog(LOG_TAG);

        private static final int RESPONSE_OK = 200;
        private String zoneId = null;

        protected String doInBackground(String... params) {
            log.d("Starting task...");
            String token = params[0];
            String accountID = params[1];

            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url("https://prod.trackmania.core.nadeo.online/accounts/" + accountID + "/zone")
                        .addHeader("Authorization", "nadeo_v1 t=" + token)
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.code() == RESPONSE_OK) {
                    String jsonData = response.body().string();
                    JSONObject Jobject = new JSONObject(jsonData);
                    zoneId = Jobject.get("zoneId").toString();
                    log.d("zoneId: " + zoneId);
                } else {
                    log.d("Response: " + response.code());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return zoneId;
        }

        @Override
        protected void onPostExecute(String zoneId) {
            super.onPostExecute(zoneId);
            //execution on main/UI thread
            player.setZoneID(zoneId, getContext());
            log.d("Player's zone is " + player.getZone());
            zoneText.setText( player.getZone() );
        }
    }


        private void checkSupportedApi(){
        String supported_api = mFirebaseRemoteConfig.getString("supported_api");
        if(API != "" && API != null && supported_api != "none" && supported_api != "") {
            log.d( "Supported API (from Firebase): " + supported_api);
            log.d( "Actual API (from Ubisoft): " + API);
            if (API.contains(supported_api)) {
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
