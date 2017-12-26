package com.italankin.flickr.http;

public class FlickrApiException extends RuntimeException {

    public static final int CODE_INVALID_API_KEY = 100;
    public static final int CODE_SERVICE_UNAVAILABLE = 105;

    private final int code;

    public FlickrApiException(AbstractResponse response) {
        super(response.message);
        code = response.code;
    }

    public int getCode() {
        return code;
    }
}
