package com.italankin.mvp.example;

import android.app.Application;

import com.italankin.mvp.example.di.component.CommonComponent;
import com.italankin.mvp.example.di.component.DaggerCommonComponent;
import com.italankin.mvp.example.di.component.DaggerPresenterComponent;
import com.italankin.mvp.example.di.component.PresenterComponent;
import com.italankin.mvp.example.di.module.CommonModule;

public class App extends Application {

    private static CommonComponent commonComponent;
    private static PresenterComponent presenterComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        commonComponent = DaggerCommonComponent.builder()
                .commonModule(new CommonModule(this))
                .build();
        presenterComponent = DaggerPresenterComponent.builder()
                .commonComponent(commonComponent)
                .build();
    }

    public static CommonComponent main() {
        return commonComponent;
    }

    public static PresenterComponent presenters() {
        return presenterComponent;
    }
}
