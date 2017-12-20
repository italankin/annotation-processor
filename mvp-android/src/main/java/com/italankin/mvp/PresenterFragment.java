package com.italankin.mvp;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Фрагмент, использующий презентер.
 *
 * @param <P> тип презентера
 */
public abstract class PresenterFragment<P extends Presenter> extends Fragment implements PresenterView {

    private P presenter;

    /**
     * Создание презентера.
     *
     * @return новый презентер
     */
    @NonNull
    protected abstract P createPresenter();

    /**
     * @return презентер
     */
    @NonNull
    public final P getPresenter() {
        return presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = createPresenter();
        super.setRetainInstance(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    @CallSuper
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attach(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public final void setRetainInstance(boolean retain) {
        // retain instance по умолчанию true
    }

}
