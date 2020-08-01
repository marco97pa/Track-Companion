package com.marco97pa.trackmania.maps;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marco97pa.trackmania.utils.FLog;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
 * GET MAP FROM MAPUID
 * HTTP GET to https://prod.trackmania.core.nadeo.online/accounts/{accountID}/trophies/lastYearSummary
 * Headers:
 *   Authorization = nadeo_v1 t={token from Auth stage 1}
 *
 * See also https://www.trackmania.com/2020/06/26/the-rankings-system-in-trackmania/
 *
 */
public class getMapfromUID extends AsyncTask<String, Void, Map> {

    private static final String LOG_TAG = "getMapfromUID";
    private FLog log = new FLog(LOG_TAG);

    private static final int RESPONSE_OK = 200;
    private Map map;

    public Map doInBackground(String... params) {
        log.d("Starting task...");
        String token = params[0];
        String mapID = params[1];

        OkHttpClient client = new OkHttpClient();

        try {
            Request request = new Request.Builder()
                    .url("https://prod.trackmania.core.nadeo.online/maps/?mapUidList=" + mapID)
                    .addHeader("Authorization", "nadeo_v1 t=" + token)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            if (response.code() == RESPONSE_OK) {
                String jsonData = response.body().string();
                Gson gson = new Gson();
                Type mapType = new TypeToken<ArrayList<Map>>() { }.getType();
                ArrayList<Map> maps = gson.fromJson(jsonData, mapType);
                map = maps.get(0);
                log.d(jsonData);
            } else {
                log.d("Response: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

}

