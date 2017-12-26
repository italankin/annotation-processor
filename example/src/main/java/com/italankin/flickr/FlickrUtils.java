package com.italankin.flickr;

import com.italankin.flickr.bean.Photo;

public final class FlickrUtils {

    public static String getPhotoUrl(Photo photo) {
        return getPhotoUrl(photo.farm, photo.server, photo.id, photo.secret);
    }

    public static String getPhotoUrl(String farmId, String serverId, String photoId, String secret) {
        return String.format("https://farm%s.staticflickr.com/%s/%s_%s_h.jpg", farmId, serverId, photoId, secret);
    }

    public static String getPhotoWebUrl(Photo photo) {
        return getPhotoWebUrl(photo.owner, photo.id);
    }

    public static String getPhotoWebUrl(String owner, String photoId) {
        return String.format("https://www.flickr.com/photos/%s/%s", owner, photoId);
    }

    public static String getProfileUrl(String owner) {
        return String.format("https://www.flickr.com/people/%s/", owner);
    }

    private FlickrUtils() {
        // no instances
    }

}
