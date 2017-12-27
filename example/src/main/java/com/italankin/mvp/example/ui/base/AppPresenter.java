package com.italankin.mvp.example.ui.base;

import com.italankin.mvp.Presenter;
import com.italankin.mvp.PresenterView;

import rx.Observer;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public abstract class AppPresenter<V extends PresenterView> extends Presenter<V> {

    protected final CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscriptions.clear();
    }

    /**
     * Base {@link Observer} implementation to reduce boilerplate code.
     *
     * @param <T> stream data type
     */
    protected abstract class AppObserver<T> implements Observer<T> {
        /**
         * Called when a new value arrives.
         *
         * @param viewState view state, as returned by {@link #getViewState()}
         * @param t         new value
         */
        protected void onNext(V viewState, T t) {
            // stub
        }

        /**
         * Called when {@link rx.Observable} encountered an error.
         *
         * @param viewState view state, as returned by {@link #getViewState()}
         * @param e         error
         */
        protected void onError(V viewState, Throwable e) {
            // stub
        }

        @Override
        public void onNext(T t) {
            onNext(getViewState(), t);
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e);
            onError(getViewState(), e);
        }

        @Override
        public void onCompleted() {
        }
    }
}
