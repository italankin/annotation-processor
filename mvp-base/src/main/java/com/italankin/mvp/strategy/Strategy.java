package com.italankin.mvp.strategy;

import com.italankin.mvp.Presenter;
import com.italankin.mvp.PresenterView;
import com.italankin.mvp.command.Command;

/**
 * Base strategy contract for view commands.
 */
public interface Strategy {

    /**
     * Apply command.
     *
     * @param presenter presenter
     * @param tag       tag of the command
     * @param command   command
     * @param <V>       view type
     */
    <V extends PresenterView> void apply(Presenter<V> presenter, Object tag, Command<V> command);

}
