package com.italankin.mvp.example.ui.main;

import com.italankin.mvp.PresenterView;
import com.italankin.mvp.annotation.Once;

import java.util.List;

interface IMainView extends PresenterView {

    void showLoading();

    void renderData(List<PhotoModelView> data);

    void renderError();

    @Once
    void renderNext(int index, int count);

}
