package com.italankin.mvp.annotation;

import com.italankin.mvp.strategy.Strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom strategy for view commands.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface CustomStrategy {

    Class<? extends Strategy> value();

}
