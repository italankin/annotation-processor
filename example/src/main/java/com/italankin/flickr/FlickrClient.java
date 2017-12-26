package com.italankin.flickr;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.italankin.flickr.bean.Photo;
import com.italankin.flickr.http.FlickrApiException;

import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Scheduler;

public class FlickrClient {

    public static final class Builder {
        private final String apiKey;
        private OkHttpClient okHttpClient;
        private Gson gson;
        private Scheduler scheduler;

        public Builder(@NonNull String apiKey) {
            this.apiKey = apiKey;
        }

        public Builder setOkHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        public Builder setGson(Gson gson) {
            this.gson = gson;
            return this;
        }

        public Builder setScheduler(@Nullable Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public FlickrClient build() {
            if (gson == null) {
                gson = new Gson();
            }
            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient();
            }
            return new FlickrClient(this);
        }
    }

    private final FlickrApi api;
    private final String apiKey;

    private FlickrClient(Builder builder) {
        apiKey = builder.apiKey;
        RxJavaCallAdapterFactory callAdapterFactory = builder.scheduler != null ?
                RxJavaCallAdapterFactory.createWithScheduler(builder.scheduler) :
                RxJavaCallAdapterFactory.create();
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.okHttpClient)
                .addCallAdapterFactory(callAdapterFactory)
                .addConverterFactory(GsonConverterFactory.create(builder.gson))
                .baseUrl("https://api.flickr.com/services/rest/")
                .validateEagerly(true)
                .build();
        api = retrofit.create(FlickrApi.class);
    }

    public Observable<List<Photo>> interestingness(@IntRange(from = 1) int page, @IntRange(from = 1, to = 100) int perPage) {
        return api.interestingness(apiKey, page, perPage)
                .map(resp -> {
                    if ("ok".equals(resp.status)) {
                        List<Photo> photos = resp.photos.photos;
                        return photos != null ? Collections.unmodifiableList(photos) : Collections.emptyList();
                    } else {
                        throw new FlickrApiException(resp);
                    }
                });
    }
}
