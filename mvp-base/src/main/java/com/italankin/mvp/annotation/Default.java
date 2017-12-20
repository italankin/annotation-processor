package com.italankin.mvp.annotation;

import com.italankin.mvp.strategy.DefaultStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Commands will similar tags will replace one another.
 *
 * @see DefaultStrategy
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Default {
}
