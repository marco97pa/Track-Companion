package com.marco97pa.trackmania.maps;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.marco97pa.trackmania.MainActivity;
import com.marco97pa.trackmania.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MapsFragment extends Fragment {

    private static final String LOG_TAG = "MapsFragment";

    private String cookie;
    private RecyclerView rvMaps;
    private ArrayList<Map> tracks;
    private MapAdapter adapter;
    private LinearLayout empty;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_maps, container, false);
        rvMaps = (RecyclerView) root.findViewById(R.id.rvMaps);
        empty = (LinearLayout) root.findViewById(R.id.empty);

        cookie = ((MainActivity) getActivity()).getCookie();

        tracks = new ArrayList<Map>();
        // Create adapter passing in the sample user data
        adapter = new MapAdapter(tracks, getActivity(), cookie);
        // Attach the adapter to the recyclerview to populate items
        rvMaps.setAdapter(adapter);
        // Set layout manager to position the items
        rvMaps.setLayoutManager(new LinearLayoutManager(getActivity()));

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(cookie == null || cookie.isEmpty()){
            Log.d(LOG_TAG, "Cookie is empty, authenticating again...");
            ((MainActivity) getActivity()).requestLogin();
            cookie =  ((MainActivity) getActivity()).getCookie();
        }

        else {
            try {
                Log.d(LOG_TAG, "Loading tracks...");
                ArrayList<Map> new_tracks = new RetrieveMapsTask().execute(cookie).get();
                if (new_tracks != null) {
                    if (new_tracks.isEmpty()){
                        Log.d(LOG_TAG, "Response is empty, authenticating again...");
                        ((MainActivity) getActivity()).requestLogin();
                        cookie =  ((MainActivity) getActivity()).getCookie();
                        onStart();
                    }
                    Log.d(LOG_TAG, "Clearing tracks...");
                    tracks.clear();
                    Log.d(LOG_TAG, "Adding new tracks...");
                    tracks.addAll(new_tracks);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


/* Da https://players.trackmania.com/player/map
    esegui document.getElementsByClassName('mp-format')[i].innerText
    trasformalo in array e caricalo in una recyclerview
 */

    public class RetrieveMapsTask extends AsyncTask<String, Void, ArrayList<Map>> {

        private static final String LOG_TAG = "RetrieveMapsTask";

        boolean network = true;

        protected ArrayList<Map> doInBackground(String... cookie) {
            Log.d(LOG_TAG, "Starting task...");
            Log.d(LOG_TAG, "Cookie: " + cookie[0]);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("Cookie", cookie[0])
                    .url("https://players.trackmania.com/player/map")
                    .build();

            try (Response response = client.newCall(request).execute()) {

                ArrayList<Map> new_tracks = new ArrayList<Map>();
                Document doc = Jsoup.parse(response.body().string());
                Elements maps = doc.select(".mp-format");
                for(Element map : maps){
                    Map track = new Map(map.text(),
                                    map.parent().attr("href"),
                                    map.parent().parent().parent().parent().select(".card-img-top").attr("src")
                                    );
                    new_tracks.add(track);
                }
                Log.d(LOG_TAG,"Fetched tracks from server");

                return new_tracks;

            } catch (IOException e) {
                e.printStackTrace();
                network = false;
            }

            return null;

        }

        @Override
        protected void onPostExecute(ArrayList<Map> new_tracks) {
            super.onPostExecute(new_tracks);

            if (new_tracks != null) {
                Log.d(LOG_TAG,"Notifying adapter of the received tracks");
                adapter.notifyDataSetChanged();
            } else {
                if(network){
                    rvMaps.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                }
                else{
                    Snackbar.make(rvMaps, getString(R.string.no_network), BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }
        }

    }


}