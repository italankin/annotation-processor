package com.italankin.mvp;

import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.Generated;

class Utils {

    static AnnotationSpec getGeneratedAnnotationSpec() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", MvpProcessor.class.getCanonicalName())
                .build();
    }

}
