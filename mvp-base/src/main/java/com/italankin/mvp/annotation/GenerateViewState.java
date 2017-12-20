package com.italankin.mvp.annotation;

import com.italankin.mvp.ViewState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to generate {@link ViewState} for {@link com.italankin.mvp.Presenter}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GenerateViewState {

    /**
     * Setting this value to true will add calls to {@link android.util.Log} when executing commands.
     *
     * @return {@code true}, if command logging enabled
     */
    boolean logging() default false;

}
