package com.italankin.mvp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Активити, сохраняющая презентер между поворотами экрана.
 *
 * @param <P> тип презентера
 */
public abstract class PresenterActivity<P extends Presenter> extends AppCompatActivity implements PresenterView {

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

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = (P) getLastCustomNonConfigurationInstance();
        if (presenter == null) {
            presenter = createPresenter();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStart() {
        super.onStart();
        presenter.attach(this);
    }

    @Override
    public final Object onRetainCustomNonConfigurationInstance() {
        return presenter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detach();
        if (isFinishing()) {
            presenter.onDestroy();
        }
    }

}
