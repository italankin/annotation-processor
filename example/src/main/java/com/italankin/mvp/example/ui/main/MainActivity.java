package com.italankin.mvp.example.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.italankin.mvp.example.App;
import com.italankin.mvp.example.R;
import com.italankin.mvp.example.ui.base.AppActivity;
import com.italankin.mvp.example.ui.main.detail.DetailActivity;
import com.italankin.mvp.example.ui.widget.LceLayout;
import com.italankin.mvp.example.ui.widget.LoadMoreListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppActivity<MainPresenter> implements IMainView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.lce)
    LceLayout lce;

    @BindView(R.id.list)
    RecyclerView list;

    @NonNull
    @Override
    protected MainPresenter createPresenter() {
        return App.presenters().main();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        list.setLayoutManager(layoutManager);
        list.addOnScrollListener(new LoadMoreListener(layoutManager, 6) {
            @Override
            public void onLoadMore() {
                getPresenter().loadNext();
            }
        });
    }

    @Override
    public void showLoading() {
        lce.showLoading();
    }

    @Override
    public void renderData(List<PhotoModelView> data) {
        PhotosAdapter adapter = new PhotosAdapter(this, data);
        adapter.setListener((modelView, position) -> {
            Intent intent = DetailActivity.getStartIntent(this, modelView.photo);
            startActivity(intent);
        });
        list.setAdapter(adapter);
        lce.showContent();
    }

    @Override
    public void renderError() {
        lce.error()
                .message(R.string.error)
                .reload(view -> getPresenter().loadNext())
                .show();
    }

    @Override
    public void renderNext(int index, int count) {
        list.getAdapter().notifyItemRangeInserted(index, count);
    }
}
