package com.italankin.mvp.example.di.component;

import android.content.Context;

import com.italankin.flickr.FlickrClient;
import com.italankin.mvp.example.di.module.CommonModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = CommonModule.class)
public interface CommonComponent {

    Context getContext();

    FlickrClient getFlickrClient();

}
