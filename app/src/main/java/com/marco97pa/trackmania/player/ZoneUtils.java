package com.marco97pa.trackmania.player;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import androidx.annotation.NonNull;

/*      HOW TO USE THIS CLASS
 *      ZoneUtils utils = new ZoneUtils();
 *      String zoneName = utils.retrieveZoneName("30200df4-7e13-11e8-8060-e284abfd2bc4", getApplicationContext());
 * */

public class ZoneUtils {

    private static String getJsonFromAssets(Context context) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open("zones.json");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }


    public String retrieveZoneName (@NonNull String query, @NonNull Context context) {
        int i;
        String zoneName = "", parentName = "";

        String jsonFileString = getJsonFromAssets(context);
        Log.d("TECCA ", "MANCIA");

        Gson gson = new Gson();
        Type zonesType = new TypeToken<List<Zone>>() { }.getType();

        List<Zone> zones = gson.fromJson(jsonFileString, zonesType);

        for(i = 0; i < zones.size(); i++){
            if(zones.get(i).getZoneId().equals(query)){
                zoneName = zones.get(i).getName();
                if(zones.get(i).getParentId() != null){
                    parentName = retrieveZoneName(zones.get(i).getParentId(), context);
                    zoneName = zoneName + " < " + parentName;
                }
            }
        }

        return zoneName;
    }

}
