package com.italankin.mvp;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

class ViewStateBundle {
    /**
     * Presenter type element
     */
    public final TypeElement presenter;
    /**
     * Generated ViewState class
     */
    public final TypeSpec viewState;

    ViewStateBundle(TypeElement presenter, TypeSpec viewState) {
        this.presenter = presenter;
        this.viewState = viewState;
    }
}
