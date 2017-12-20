package com.italankin.mvp;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.italankin.mvp.command.Command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @param <V> view type
 */
public abstract class Presenter<V extends PresenterView> {

    private final V viewState;
    private V view;
    private boolean firstAttach = true;

    private final LinkedHashMap<Object, Command<? super V>> commands = new LinkedHashMap<>(1);
    private final ArrayDeque<Command<? super V>> oneTimeCommands = new ArrayDeque<>(1);

    @SuppressWarnings("unchecked")
    public Presenter() {
        viewState = (V) MvpProvider.getViewState(this);
    }

    /**
     * Attach presenter to {@code view}.
     *
     * @param view view
     * @see #detach()
     */
    @UiThread
    @CallSuper
    public void attach(@NonNull V view) {
        if (this.view == view) {
            return;
        }
        this.view = view;
        if (firstAttach) {
            firstAttach = false;
            onFirstAttach(viewState != null ? viewState : view);
        } else {
            onRestoreState(view);
        }
    }

    /**
     * Callen when {@code view} is attached for the first time.
     *
     * @param view view
     * @see #attach(PresenterView)
     */
    @UiThread
    protected void onFirstAttach(@NonNull V view) {
    }

    /**
     * Called when presenter restores view state.
     *
     * @param view View
     * @see #attach(PresenterView)
     */
    @UiThread
    @CallSuper
    protected void onRestoreState(@NonNull V view) {
        replayCommands(view);
    }

    /**
     * Detach view from the presenter.
     */
    @UiThread
    @CallSuper
    public void detach() {
        view = null;
    }

    /**
     * Called when view is destroyed.
     */
    @CallSuper
    public void onDestroy() {
        cancelAll();
        firstAttach = true;
    }

    /**
     * @return attached view
     */
    @Nullable
    public final V getView() {
        return view;
    }

    /**
     * Returns view state which used to send commands to view.
     *
     * @return view state
     */
    public final V getViewState() {
        return viewState;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Commands
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Send a command.
     *
     * @param command command
     */
    @UiThread
    public final void send(@NonNull Command<? super V> command) {
        send(defaultTag(), command);
    }

    /**
     * Send a command, which will be executed once.
     *
     * @param command command
     */
    @UiThread
    public final void sendOnce(@NonNull Command<? super V> command) {
        send(null, command);
    }

    /**
     * Send a command to a view with {@code tag}.
     *
     * @param tag     command tag, can be {@code null}
     * @param command command
     */
    @UiThread
    public final void send(@Nullable Object tag, @NonNull Command<? super V> command) {
        if (tag != null) {
            commands.put(tag, command);
        }
        V view = getView();
        if (view != null) {
            command.call(view);
        } else if (tag == null) {
            oneTimeCommands.offer(command);
        }
    }

    /**
     * Remove command with {@code tag}.
     *
     * @param tag command tag
     */
    @UiThread
    public final void cancel(@NonNull Object tag) {
        commands.remove(tag);
    }

    /**
     * Remove command sent via {@link #send(Command)}.
     */
    @UiThread
    public final void cancel() {
        cancel(defaultTag());
    }

    /**
     * Remove all commands.
     */
    public final void cancelAll() {
        commands.clear();
    }

    /**
     * @return current command list
     */
    public List<Command<? super V>> getCommands() {
        return new ArrayList<>(commands.values());
    }

    /**
     * @return default tag for commands
     * @see #send(Command)
     */
    public Object defaultTag() {
        return this;
    }

    /**
     * Replay commands to {@code view}.
     *
     * @param view view
     */
    @UiThread
    private void replayCommands(V view) {
        Collection<Command<? super V>> values = commands.values();
        for (Command<? super V> command : values) {
            command.call(view);
        }
        Command<? super V> command;
        while ((command = oneTimeCommands.poll()) != null) {
            command.call(view);
        }
    }

}
