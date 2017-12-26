package com.italankin.mvp.example.di.module;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.italankin.flickr.FlickrClient;
import com.italankin.mvp.example.App;
import com.italankin.mvp.example.BuildConfig;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@Module
public class CommonModule {

    private final App app;

    public CommonModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.followRedirects(false);
        if (BuildConfig.DEBUG) {
            // add interceptor to log network activity
            try {
                Class<?> klass = Class.forName("com.italankin.mvp.example.DebugInterceptor");
                Interceptor interceptor = (Interceptor) klass.newInstance();
                builder.addInterceptor(interceptor);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return builder.build();
    }

    @Provides
    @Singleton
    public FlickrClient provideFlickrClient(OkHttpClient okHttpClient) {
        return new FlickrClient.Builder(BuildConfig.FLICKR_API_KEY)
                .setGson(BuildConfig.DEBUG ? new GsonBuilder().setPrettyPrinting().create() : new Gson())
                .setOkHttpClient(okHttpClient)
                .setScheduler(Schedulers.io())
                .build();
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return app;
    }

}
