package com.marco97pa.trackmania.player;

public class Player {
    private String nickname;
    private String image;
    private String api_version;

    public Player(String nickname, String image, String api_version){
        this.nickname = nickname;
        this.image = image;
        this.api_version = api_version;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getApi_version() {
        return api_version;
    }
}
