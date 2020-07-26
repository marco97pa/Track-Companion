package com.marco97pa.trackmania.player;

import android.content.Context;

public class Player {
    private String accountID;
    private String username;
    private String zoneID;
    private String zone;

    public Player(String accountID, String username){
        this.accountID = accountID;
        this.username = username;
    }

    public String getAccountID() {
        return accountID;
    }

    public String getUsername() {
        return username;
    }

    public void setZoneID(String zoneID, Context context) {
        this.zoneID = zoneID;
        ZoneUtils utils = new ZoneUtils();
        this.zone = utils.retrieveZoneName(zoneID, context);
    }

    public String getZone() {
        return zone;
    }
}
