package com.italankin.mvp.command;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.italankin.mvp.PresenterView;

/**
 * View command.
 *
 * @param <V> view type
 */
public interface Command<V extends PresenterView> {
    /**
     * Execute view command on {@code view}.
     *
     * @param view view
     */
    @UiThread
    void call(@NonNull V view);
}
