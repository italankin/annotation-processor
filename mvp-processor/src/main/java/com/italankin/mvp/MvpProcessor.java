package com.italankin.mvp;

import com.google.auto.service.AutoService;
import com.italankin.mvp.annotation.GenerateViewState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.italankin.mvp.annotation.GenerateViewState")
public class MvpProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Messager messager;
    private ProcessingEnvironment processingEnv;
    /**
     * Presenter type declared as {@code Presenter<? extends PresenterView>}
     */
    private DeclaredType presenterType;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        processingEnv = env;
        typeUtils = env.getTypeUtils();
        Elements elementUtils = env.getElementUtils();
        messager = env.getMessager();
        WildcardType presenterViewType = typeUtils.getWildcardType(
                elementUtils.getTypeElement(PresenterView.class.getCanonicalName()).asType(), null);
        presenterType = typeUtils.getDeclaredType(
                elementUtils.getTypeElement(Presenter.class.getCanonicalName()), presenterViewType);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Process
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        List<ViewStateBundle> bundles = new ArrayList<>();

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GenerateViewState.class);
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            TypeElement viewType = getViewType(typeElement);
            if (viewType == null) {
                continue;
            }
            ViewStateGenerator viewStateGenerator = new ViewStateGenerator(
                    processingEnv, typeElement, viewType);
            bundles.add(new ViewStateBundle(typeElement, viewStateGenerator.generate()));
        }

        MvpProviderGenerator mvpProviderGenerator = new MvpProviderGenerator(processingEnv, bundles);
        mvpProviderGenerator.generate();

        return true;
    }

    private TypeElement getViewType(TypeElement type) {
        // check if we have actually a subtype of Presenter class
        if (!typeUtils.isSubtype(type.getSuperclass(), presenterType)) {
            error(type, "%s can only be applied to objects of type %s", GenerateViewState.class.getCanonicalName(),
                    Presenter.class.getCanonicalName());
            return null;
        }
        // to get a type parameter of presenter, we need first to get parameterized type of Presenter class
        DeclaredType presenterSupertype = (DeclaredType) getPresenterParameterizedType(type.asType());
        if (presenterSupertype == null) {
            return null;
        }
        // now we need to check actual PresenterView parameter
        List<? extends TypeMirror> typeParameters = presenterSupertype.getTypeArguments();
        // actual type of Presenter type parameter
        DeclaredType viewTypeParameter = (DeclaredType) typeParameters.get(0); // only one parameter
        // check if it's an interface
        TypeElement viewType = (TypeElement) viewTypeParameter.asElement();
        if (viewType.getKind() != ElementKind.INTERFACE) {
            error(type, "Type parameter '%s' must be an interface", viewType.getSimpleName().toString());
            return null;
        }
        if (!viewType.getTypeParameters().isEmpty()) {
            error(viewType, "%s must not have type parameters", viewType.getSimpleName().toString());
            return null;
        }
        return viewType;
    }

    /**
     * Find actual Presenter type such as {@code Presenter<MyView>}
     */
    private TypeMirror getPresenterParameterizedType(TypeMirror type) {
        // classes come before interfaces, so we get 0th element from array
        TypeMirror superclass = typeUtils.directSupertypes(type).get(0);
        TypeElement element = (TypeElement) typeUtils.asElement(superclass);
        // search for Presenter type
        if (Presenter.class.getCanonicalName().equals(
                element.getQualifiedName().toString())) {
            return superclass;
        } else {
            // go up in the class heirarchy
            return getPresenterParameterizedType(superclass);
        }
        // since we checked for class extends Presenter earlier, we don't need to check Object type
    }
}
