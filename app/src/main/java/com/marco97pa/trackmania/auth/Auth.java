package com.marco97pa.trackmania.auth;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

import java.util.Date;

/*  AUTH CLASS
    Collects all the authentication info such as:
    - String accessToken: access token, used to authenticate with the game APIs
    - String refreshToken: refresh token, used to renew the access token when it expires
    @see https://github.com/The-Firexx/trackmania2020apidocumentation/blob/master/Login.md
    - JWT jwt: contains the JWT informations of authentication
    @see https://jwt.io/
 */

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
        return jwt.isExpired(1); // 1 second leeway
    }

    public long getExpireTime(){
        Date expires_at = jwt.getExpiresAt();
        Date now = new Date();
        long diffInMillis = expires_at.getTime() - now.getTime();
        return diffInMillis;
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

