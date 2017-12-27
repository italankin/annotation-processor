package com.italankin.mvp.example.ui.widget;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class LoadMoreListener extends RecyclerView.OnScrollListener {

    private final LinearLayoutManager mLayoutManager;
    private final int mVisibleThreshold;

    public LoadMoreListener(LinearLayoutManager linearLayoutManager, int visibleThreshold) {
        mLayoutManager = linearLayoutManager;
        mVisibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy <= 0) {
            return;
        }
        int totalItemCount = mLayoutManager.getItemCount();
        if (totalItemCount <= 0) {
            return;
        }

        int visibleItemCount = recyclerView.getChildCount();
        int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

        if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + mVisibleThreshold)) {
            onLoadMore();
        }
    }

    public abstract void onLoadMore();

}