package com.italankin.mvp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify tag for command. Commands which share tags will replace each other.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Tag {

    /**
     * For one-time commands
     */
    String NO_TAG = "";

    String value() default NO_TAG;

}
