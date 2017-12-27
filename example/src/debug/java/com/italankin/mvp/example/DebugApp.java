package com.italankin.mvp.example;

import timber.log.Timber;

public class DebugApp extends App {
    @Override
    public void onCreate() {
        Timber.plant(new Timber.DebugTree());
        super.onCreate();
    }
}
