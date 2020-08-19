package com.marco97pa.trackmania.dashboard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marco97pa.trackmania.MainActivity;
import com.marco97pa.trackmania.R;
import com.marco97pa.trackmania.maps.Map;
import com.marco97pa.trackmania.maps.MapAdapter;
import com.marco97pa.trackmania.utils.FLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/*
    DashboardFragment
    Shows the active season and its details
 */

public class DashboardFragment extends Fragment {

    private static final String LOG_TAG = "DashboardFragment";
    private FLog log = new FLog(LOG_TAG);
    private String nadeoToken = null; //ACCESSTOKEN v2 from NADEO
    private TextView seasonNameText;
    private TextView seasonDetailsText;
    private ProgressBar progressBar;
    private Season season;
    private RecyclerView rvMaps;
    private ArrayList<Map> maps;
    private MapAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        seasonNameText = root.findViewById(R.id.seasonName);
        seasonDetailsText = root.findViewById(R.id.seasonDetails);
        progressBar = root.findViewById(R.id.progressBar);
        rvMaps = (RecyclerView) root.findViewById(R.id.rvMaps);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = root.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        maps = new ArrayList<Map>();
        // Create adapter passing in the sample user data
        adapter = new MapAdapter(maps, getActivity());
        // Attach the adapter to the recyclerview to populate items
        rvMaps.setAdapter(adapter);
        // Set layout manager to position the items
        rvMaps.setLayoutManager(new LinearLayoutManager(getActivity()));

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        season = ((MainActivity)getActivity()).getSeason();
        if(season == null) {
            try {
                nadeoToken = new getNadeoToken().execute(
                        ((MainActivity) getActivity()).getAuth().getAccessToken()).get();
                season = new getSeason().execute(nadeoToken).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            seasonNameText.setText(season.getName());
            seasonDetailsText.setText(season.getRankingsAsString());
            if(adapter.getItemCount() == 0) {
                maps.clear();
                maps.addAll(season.getMaps());
            }
        }
    }

    /*
     * Stage TWO: GET A TOKEN for NADEOSERVICES
     * HTTP POST to https://prod.trackmania.core.nadeo.online/v2/authentication/token/nadeoservices needed:
     * Authorization = <ticket from Stage ONE>
     *
     * Returns an accessToken value and a refreshToken value
     */
    public class getNadeoToken extends AsyncTask<String, Void, String> {

        private static final String LOG_TAG = "getNadeoToken";
        private FLog log = new FLog(LOG_TAG);

        private static final int RESPONSE_OK = 200;
        private String accessToken = null;

        protected String doInBackground(String... params) {
            log.d("Starting task...");
            String token = params[0];

            log.d("Starting STAGE 2");

            OkHttpClient client = new OkHttpClient();

            try {
                Request request = new Request.Builder()
                        .url("https://prod.trackmania.core.nadeo.online/v2/authentication/token/nadeoservices")
                        .addHeader("Authorization", "nadeo_v1 t=" + token)
                        .post(RequestBody.create("{\"audience\": \"NadeoLiveServices\"}", MediaType.parse("application/json")))
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if(response.code() == RESPONSE_OK){
                    String jsonData = response.body().string();
                    JSONObject Jobject = new JSONObject(jsonData);
                    accessToken = Jobject.get("accessToken").toString();
                    log.d("accessToken: " + accessToken);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }


            return  accessToken;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
    }


    /*
     * GET ACTIVE SEASON
     * HTTP GET to https://live-services.trackmania.nadeo.live/api/token/campaign/official?offset=0&length=1
     * Headers:
     *   Authorization = nadeo_v1 t={token from Auth stage 2}
     *
     */
    public class getSeason extends AsyncTask<String, Void, Season> {

        private static final String LOG_TAG = "getNadeoToken";
        private FLog log = new FLog(LOG_TAG);

        private static final int RESPONSE_OK = 200;


        protected Season doInBackground(String... params) {
            log.d("Starting task...");
            String accessToken = params[0];

             String seasonName = null;
             String seasonUid = null;
             ArrayList<String> mapUIDs = new ArrayList<String>();

            OkHttpClient client = new OkHttpClient();

            try {
                Request request = new Request.Builder()
                        .url("https://live-services.trackmania.nadeo.live/api/token/campaign/official?offset=0&length=1")
                        .addHeader("Authorization", "nadeo_v1 t=" + accessToken)
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.code() == RESPONSE_OK) {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    int i,j;
                    JSONArray campaignList = jsonObject.getJSONArray("campaignList");
                    for (i = 0; i < campaignList.length(); i++) {
                        if (campaignList.getJSONObject(i).get("published").toString() == "true") {
                            seasonName = campaignList.getJSONObject(i).get("name").toString();
                            log.d("seasonName: " + seasonName);
                            seasonUid = campaignList.getJSONObject(i).get("seasonUid").toString();
                            log.d("seasonUid: " + seasonUid);
                            JSONArray maps = campaignList.getJSONObject(i).getJSONArray("playlist");
                            for (j = 0; j < maps.length(); j++) {
                                String mapUID = maps.getJSONObject(j).get("mapUid").toString();
                                log.d("mapUID: "+ mapUID);
                                mapUIDs.add(mapUID);
                            }
                        }
                    }
                } else {
                    log.d("Response: " + response.code());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            //Get rankings about that season
            ArrayList<String> rankings = new ArrayList<String>();

            try {
                Request request = new Request.Builder()
                        .url("https://live-services.trackmania.nadeo.live/api/token/leaderboard/group/" + seasonUid + "/")
                        .addHeader("Authorization", "nadeo_v1 t=" + accessToken)
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.code() == RESPONSE_OK) {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    int i;
                    JSONArray zones = jsonObject.getJSONArray("zones");
                    for (i = 0; i < zones.length(); i++) {
                        String zoneName = zones.getJSONObject(i).get("zoneName").toString();
                        String position = zones.getJSONObject(i).getJSONObject("ranking").get("position").toString();
                        String length = zones.getJSONObject(i).getJSONObject("ranking").get("length").toString();

                        rankings.add(zoneName + ": " + position);
                    }
                } else {
                    log.d("Response: " + response.code());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            //Get scores and rankings on every map of that season
            try {
                Request request = new Request.Builder()
                        .url("https://live-services.trackmania.nadeo.live/api/token/leaderboard/group/" + seasonUid + "/map")
                        .addHeader("Authorization", "nadeo_v1 t=" + accessToken)
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.code() == RESPONSE_OK) {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray maps = jsonObject.names();
                    int i,j;
                    for (i = 0; i < maps.length(); i++) {
                        String mapUID = maps.getJSONObject(i).getString("mapUid");
                        String score = maps.getJSONObject(i).getString("score");
                        log.d("mapUID " + mapUID);
                        log.d("score " + score);
                        ArrayList<String> map_rankings = new ArrayList<String>();
                        JSONArray zones = maps.getJSONObject(i).getJSONArray("zones");
                        for (j = 0; j < zones.length(); j++) {
                            String zoneName = zones.getJSONObject(j).get("zoneName").toString();
                            String position = zones.getJSONObject(j).getJSONObject("ranking").get("position").toString();
                            String length = zones.getJSONObject(j).getJSONObject("ranking").get("length").toString();

                            map_rankings.add(zoneName + ": " + position);
                            log.d("rank " + zoneName + ": " + position);
                        }
                    }
                } else {
                    log.d("Response: " + response.code());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            Season season = new Season(seasonUid, seasonName, rankings, mapUIDs);
            return season;

        }

        @Override
        protected void onPostExecute(Season season) {
            super.onPostExecute(season);
            //execution on main/UI thread
            seasonNameText.setText(season.getName());
            seasonDetailsText.setText(season.getRankingsAsString());

            maps.clear();
            ArrayList<String> params = new ArrayList<>();
            params.add(((MainActivity)getActivity()).getAuth().getAccessToken());
            params.addAll(season.getMapUIDs());
                try {
                    ArrayList<Map> new_maps = new getMapsfromUID().execute(
                            params).get();
                    maps.addAll(new_maps);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            season.setMaps(maps);
            ((MainActivity)getActivity()).setSeason(season);

        }
    }

    /*
     * GET MAPs FROM MAPUID
     * HTTP GET to https://prod.trackmania.core.nadeo.online/accounts/{accountID}/trophies/lastYearSummary
     * Headers:
     *   Authorization = nadeo_v1 t={token from Auth stage 1}
     *
     * See also https://www.trackmania.com/2020/06/26/the-rankings-system-in-trackmania/
     *
     */
    public class getMapsfromUID extends AsyncTask<ArrayList<String>, Integer, ArrayList<Map>> {

        private static final String LOG_TAG = "getMapsfromUID";
        private FLog log = new FLog(LOG_TAG);

        private static final int RESPONSE_OK = 200;
        private ArrayList<Map> maps = new ArrayList<>();

        public ArrayList<Map> doInBackground(ArrayList<String>... params) {
            log.d("Starting task...");
            String token = params[0].get(0);

            int i;

            OkHttpClient client = new OkHttpClient();

            for(i = 1; i < params[0].size(); i++) {

                try {
                    Request request = new Request.Builder()
                            .url("https://prod.trackmania.core.nadeo.online/maps/?mapUidList=" + params[0].get(i))
                            .addHeader("Authorization", "nadeo_v1 t=" + token)
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    if (response.code() == RESPONSE_OK) {
                        String jsonData = response.body().string();
                        Gson gson = new Gson();
                        Type mapType = new TypeToken<ArrayList<Map>>() {
                        }.getType();
                        ArrayList<Map> temp = gson.fromJson(jsonData, mapType);
                        maps.add(temp.get(0));
                        log.d(jsonData);
                    } else {
                        log.d("Response: " + response.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return maps;
        }

        @Override
        protected void onPostExecute(ArrayList<Map> maps) {
            super.onPostExecute(maps);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

    }



}
