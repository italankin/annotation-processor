package com.italankin.mvp;

import android.support.annotation.Keep;

public abstract class ViewState<T extends PresenterView> {

    protected final Presenter<T> presenter;

    @Keep
    public ViewState(Presenter<T> presenter) {
        this.presenter = presenter;
    }
}
