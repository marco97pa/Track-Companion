package com.marco97pa.trackmania.maps;

import android.util.Log;

import com.marco97pa.trackmania.utils.FLog;

public class Map {
    private static final String LOG_TAG = "Map";
    private FLog log = new FLog(LOG_TAG);
    private String title;
    private String link;
    private String image;

    public Map(String title, String link, String image){
        this.title = title;
        this.link = link;
        this.image = image;
        log.d( "Adding new track...");
        Log.d(LOG_TAG + " Title", title);
        Log.d(LOG_TAG + " Link", link);
        Log.d(LOG_TAG + " Image", image);
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getImage() {
        return image;
    }

}
