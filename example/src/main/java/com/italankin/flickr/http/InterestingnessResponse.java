package com.italankin.flickr.http;

import com.google.gson.annotations.SerializedName;
import com.italankin.flickr.bean.Photo;

import java.util.List;

public class InterestingnessResponse extends AbstractResponse {

    @SerializedName("photos")
    public Photos photos;

    public static class Photos {

        @SerializedName("page")
        public int page;

        @SerializedName("pages")
        public int pages;

        @SerializedName("perpage")
        public int perPage;

        @SerializedName("total")
        public int total;

        @SerializedName("photo")
        public List<Photo> photos;

    }

}
