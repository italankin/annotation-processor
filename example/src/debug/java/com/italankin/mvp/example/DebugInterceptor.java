package com.italankin.mvp.example;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

@SuppressWarnings("unused") // used reflectively
public class DebugInterceptor implements Interceptor {

    private final HttpLoggingInterceptor interceptor;

    public DebugInterceptor() {
        interceptor = new HttpLoggingInterceptor(message -> {
            Log.d("NETWORK", message);
        });
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return interceptor.intercept(chain);
    }
}
