package com.italankin.flickr.http;

import com.google.gson.annotations.SerializedName;

abstract class AbstractResponse {

    @SerializedName("stat")
    public String status;

    @SerializedName("code")
    public int code;

    @SerializedName("message")
    public String message;

}
