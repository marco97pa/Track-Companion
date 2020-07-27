package com.marco97pa.trackmania.auth;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;


public class Auth {
    private String accessToken;
    private JWT jwt;
    private String refreshToken;

    public Auth(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.jwt = new JWT(accessToken);
        this.refreshToken = refreshToken;
    }


    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean isExpired() {
        return jwt.isExpired(10); // 10 seconds leeway
    }

    public String getUsername(){
        Claim claim = jwt.getClaim("aun");
        return claim.asString();
    }

    public String getAccountId(){
        Claim claim = jwt.getClaim("sub");
        return claim.asString();
    }

    public String getAPIversion(){
        return jwt.getHeader().get("ver");
    }
}

