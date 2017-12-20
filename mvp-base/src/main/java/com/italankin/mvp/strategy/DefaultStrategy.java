package com.italankin.mvp.strategy;

import com.italankin.mvp.Presenter;
import com.italankin.mvp.PresenterView;
import com.italankin.mvp.command.Command;

public class DefaultStrategy implements Strategy {

    @Override
    public <V extends PresenterView> void apply(Presenter<V> presenter, Object tag, Command<V> command) {
        presenter.send(tag, command);
    }

}
