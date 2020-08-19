package com.marco97pa.trackmania.maps;

/*
    MAP
    A map (or track) is an in-game circuit.
    It is defined by:
    - String id
    - String Uid
    - String name, the name of the track
    - String thumbnailUrl, an URl that points to a JPG image of the track
    - String fileURL, an URL that points to the download link of the track
    - String timestamp, time when the track was created
    - String author, who created the track
    - String bronzeScore, silverScore, goldScore, authorScore, scores of each medal
 */

public class Map {
    private String mapUid;
    private String mapId;
    private String name;
    private String thumbnailUrl;
    private String fileUrl;
    private String timestamp;
    private String bronzeScore;
    private String silverScore;
    private String goldScore;
    private String authorScore;
    private String author;

    public String getMapUid() {
        return mapUid;
    }

    public void setMapUid(String mapUid) {
        this.mapUid = mapUid;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBronzeScore() {
        return bronzeScore;
    }

    public void setBronzeScore(String bronzeScore) {
        this.bronzeScore = bronzeScore;
    }

    public String getSilverScore() {
        return silverScore;
    }

    public void setSilverScore(String silverScore) {
        this.silverScore = silverScore;
    }

    public String getGoldScore() {
        return goldScore;
    }

    public void setGoldScore(String goldScore) {
        this.goldScore = goldScore;
    }

    public String getAuthorScore() {
        return authorScore;
    }

    public void setAuthorScore(String authorScore) {
        this.authorScore = authorScore;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
