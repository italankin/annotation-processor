package com.italankin.mvp.annotation;

import com.italankin.mvp.strategy.OnceStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command will be executed only once.
 *
 * @see OnceStrategy
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Once {
}
