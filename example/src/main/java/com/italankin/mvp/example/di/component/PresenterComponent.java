package com.italankin.mvp.example.di.component;

import com.italankin.mvp.example.di.scope.AppScope;
import com.italankin.mvp.example.ui.main.MainPresenter;

import dagger.Component;

@AppScope
@Component(dependencies = CommonComponent.class)
public interface PresenterComponent {

    MainPresenter main();

}
