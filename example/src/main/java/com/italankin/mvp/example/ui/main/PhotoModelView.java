package com.italankin.mvp.example.ui.main;

import com.italankin.flickr.bean.Photo;

class PhotoModelView {
    public final String id;
    public final String url;
    public final String viewsCount;
    public final Photo photo;

    PhotoModelView(Photo photo) {
        this.photo = photo;
        id = photo.id;
        url = photo.urlLarge;
        viewsCount = String.valueOf(photo.views);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
