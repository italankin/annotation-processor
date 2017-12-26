package com.italankin.flickr.bean;

import com.google.gson.annotations.SerializedName;

public class Photo {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("url_l")
    public String urlLarge;

    @SerializedName("ownername")
    public String ownerName;

    @SerializedName("owner")
    public String owner;

    @SerializedName("server")
    public String server;

    @SerializedName("secret")
    public String secret;

    @SerializedName("farm")
    public String farm;

    @SerializedName("views")
    public long views;

}
