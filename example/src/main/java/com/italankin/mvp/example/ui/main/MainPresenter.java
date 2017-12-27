package com.italankin.mvp.example.ui.main;

import android.support.annotation.NonNull;

import com.italankin.flickr.FlickrClient;
import com.italankin.mvp.annotation.GenerateViewState;
import com.italankin.mvp.example.di.scope.AppScope;
import com.italankin.mvp.example.ui.base.AppPresenter;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

@AppScope
@GenerateViewState
public class MainPresenter extends AppPresenter<IMainView> {

    private static final int COUNT = 30;

    private final FlickrClient flickrClient;

    private List<PhotoModelView> data;

    private Subscription loadSub;
    private boolean done = false;

    @Inject
    public MainPresenter(FlickrClient flickrClient) {
        this.flickrClient = flickrClient;
    }

    @Override
    protected void onFirstAttach(@NonNull IMainView view) {
        loadNext();
    }

    void loadNext() {
        if (!hasMore()) {
            return;
        }
        int size = data == null || data.isEmpty() ? 0 : data.size();
        if (size == 0) {
            load();
        } else {
            loadNext(size / COUNT + 1);
        }
    }

    boolean hasMore() {
        return !done && (loadSub == null || loadSub.isUnsubscribed());
    }

    private void load() {
        if (data == null || data.isEmpty()) {
            getViewState().showLoading();
        }
        loadSub = getInterestingPhotos(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppObserver<List<PhotoModelView>>() {
                    @Override
                    protected void onNext(IMainView viewState, List<PhotoModelView> photos) {
                        viewState.renderData(data = photos);
                    }

                    @Override
                    protected void onError(IMainView viewState, Throwable e) {
                        viewState.renderError();
                    }
                });
        subscriptions.add(loadSub);
    }

    private void loadNext(int page) {
        loadSub = getInterestingPhotos(page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppObserver<List<PhotoModelView>>() {
                    @Override
                    protected void onNext(IMainView viewState, List<PhotoModelView> photos) {
                        int index = data.size();
                        int count = photos.size();
                        data.addAll(photos);
                        done = count < COUNT;
                        viewState.renderNext(index, count);
                    }

                    @Override
                    protected void onError(IMainView viewState, Throwable e) {
                        viewState.renderError();
                    }
                });
        subscriptions.add(loadSub);
    }

    private Observable<List<PhotoModelView>> getInterestingPhotos(int page) {
        return flickrClient.interestingness(page, COUNT)
                .concatMapIterable(photos -> photos)
                .map(PhotoModelView::new)
                .toList();
    }
}
