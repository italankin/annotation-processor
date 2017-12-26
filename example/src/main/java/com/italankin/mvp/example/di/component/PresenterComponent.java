package com.italankin.mvp.example.di.component;

import com.italankin.mvp.example.di.scope.AppScope;

import dagger.Component;

@AppScope
@Component(dependencies = CommonComponent.class)
public interface PresenterComponent {
}
