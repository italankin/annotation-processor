package com.italankin.mvp;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class MvpProviderGenerator {
    private final ProcessingEnvironment env;
    private final List<ViewStateBundle> bundles;

    public MvpProviderGenerator(ProcessingEnvironment processingEnv, List<ViewStateBundle> bundles) {
        this.env = processingEnv;
        this.bundles = bundles;
    }

    public void generate() {
        TypeSpec.Builder type = TypeSpec.classBuilder("MvpProvider")
                .addModifiers(Modifier.FINAL)
                .addAnnotation(Utils.getGeneratedAnnotationSpec());

        MethodSpec.Builder method = MethodSpec.methodBuilder("getViewState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Object.class, "object")
                .returns(Object.class)
                .addStatement("$T presenter = ($T) object", Presenter.class, Presenter.class)
                .addStatement("$T klass = presenter.getClass()", WildcardTypeName.get(Class.class));

        // build ifs
        CodeBlock.Builder builder = CodeBlock.builder();
        for (ViewStateBundle bundle : bundles) {
            builder.beginControlFlow("if (klass == $T.class)", TypeName.get(bundle.presenter.asType()));
            String packageName = env.getElementUtils().getPackageOf(bundle.presenter).getQualifiedName().toString();
            ClassName viewState = ClassName.get(packageName, bundle.viewState.name);
            builder.addStatement("return new $T(($T) presenter)", viewState, ClassName.get(bundle.presenter));
            builder.endControlFlow();
        }

        // throw exception, if we have no match
        builder.addStatement("throw new $T($S + klass.getCanonicalName())",
                RuntimeException.class, "Cannot find view state for: ");
        method.addCode(builder.build());
        type.addMethod(method.build());

        JavaFile javaFile = JavaFile.builder("com.italankin.mvp", type.build())
                .indent("    ")
                .build();
        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
