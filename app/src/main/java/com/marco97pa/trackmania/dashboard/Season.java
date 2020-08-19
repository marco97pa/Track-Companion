package com.marco97pa.trackmania.dashboard;

import com.marco97pa.trackmania.maps.Map;

import java.util.ArrayList;

/*
    Season
    A season is an in-game collection of maps. It is defined by a uid, a name, its rankings and maps
 */
public class Season {
    private String uid;
    private String name;
    private ArrayList<String> rankings;
    private ArrayList<String> mapUIDs;
    private ArrayList<Map> maps;

    public Season(String uid, String name, ArrayList<String> rankings, ArrayList<String> mapUIDs){
        this.uid = uid;
        this.name = name;
        this.rankings = rankings;
        this.mapUIDs = mapUIDs;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getRankings() {
        return rankings;
    }

    public String getRankingsAsString() {
        return rankings.toString().substring(1 , rankings.toString().length() - 1);
    }

    public void setRankings(ArrayList<String> rankings) {
        this.rankings = rankings;
    }

    public ArrayList<String> getMapUIDs() {
        String alluids = "";
        for (String uid: mapUIDs) {
            alluids = uid + "%";
        }
        return mapUIDs;
    }


    public void setMapUIDs(ArrayList<String> mapUIDs) {
        this.mapUIDs = mapUIDs;
    }

    public ArrayList<Map> getMaps() {
        return maps;
    }

    public void setMaps(ArrayList<Map> maps) {
        this.maps = maps;
    }
}
