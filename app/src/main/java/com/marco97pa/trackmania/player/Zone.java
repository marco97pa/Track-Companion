package com.marco97pa.trackmania.player;

import android.content.Context;

import android.util.Log;

import java.io.IOException;
import java.util.List;


import androidx.annotation.NonNull;

/*
    Zone
    a Zone of the World in the game
 */

public class Zone {

    private String icon;
    private String name;
    private String parentId;
    private String zoneId;

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public String getZoneId() {
        return zoneId;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "name='" + name + '\'' +
                ", zoneId='" + zoneId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
