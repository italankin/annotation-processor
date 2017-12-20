package com.italankin.mvp;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.italankin.mvp.annotation.CustomStrategy;
import com.italankin.mvp.annotation.Default;
import com.italankin.mvp.annotation.GenerateViewState;
import com.italankin.mvp.annotation.Once;
import com.italankin.mvp.annotation.Single;
import com.italankin.mvp.annotation.Tag;
import com.italankin.mvp.command.Command;
import com.italankin.mvp.strategy.DefaultStrategy;
import com.italankin.mvp.strategy.OnceStrategy;
import com.italankin.mvp.strategy.SingleStrategy;
import com.italankin.mvp.strategy.Strategy;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class ViewStateGenerator {

    private static final Set<String> FORBIDDEN_PARAMETER_NAMES;

    static {
        FORBIDDEN_PARAMETER_NAMES = new HashSet<>(2);
        FORBIDDEN_PARAMETER_NAMES.add("view");
        FORBIDDEN_PARAMETER_NAMES.add("presenter");
    }

    private final ProcessingEnvironment env;
    private final TypeElement annotatedElement;
    private final TypeElement viewInterface;

    public ViewStateGenerator(ProcessingEnvironment processingEnv, TypeElement annotatedElement, TypeElement viewInterface) {
        this.env = processingEnv;
        this.annotatedElement = annotatedElement;
        this.viewInterface = viewInterface;
    }

    public TypeSpec generate() {
        String viewStateClassName = annotatedElement.getSimpleName() + "$$ViewState";
        List<ExecutableElement> enclosedMethods = new ArrayList<>();
        for (Element enclosedElement : viewInterface.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                enclosedMethods.add((ExecutableElement) enclosedElement);
            }
        }
        Set<StrategyMirror> strategies = new HashSet<>();
        List<MethodSpec> methodSpecs = new ArrayList<>();
        Map<String, TypeSpec> commandTypeSpecs = new LinkedHashMap<>();
        // implement methods
        for (ExecutableElement method : enclosedMethods) {
            if (method.getModifiers().contains(Modifier.STATIC)) {
                // skip static methods
                continue;
            }

            if (!method.getTypeParameters().isEmpty()) {
                env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "View command methods must not have type parameters", method);
                return null;
            }

            String methodName = method.getSimpleName().toString();
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC);

            // add method parameters
            if (!addMethodParameters(method, methodBuilder)) {
                // some error occured
                return null;
            }

            TypeMirror returnType = method.getReturnType();

            // view commands
            // accept only void methods
            if (returnType.getKind() != TypeKind.VOID) {
                env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method must return void", method);
                return null;
            }
            methodBuilder.returns(TypeName.VOID);

            TypeSpec command = createCommandSpec(method, viewStateClassName);
            if (commandTypeSpecs.containsKey(command.name)) {
                env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method names should be unique", method);
                return command;
            }
            commandTypeSpecs.put(command.name, command);

            StrategyMirror strategy = resolveStrategy(method);
            if (strategy == null) {
                // some error occured
                return command;
            }
            if (!strategies.contains(strategy)) {
                strategies.add(strategy);
            }
            methodBuilder.addStatement("$L.apply($L, $S, new $N($L))",
                    strategyRefName(strategy), "presenter", getCommandTag(method), command, methodArgs(method));

            // add method
            methodSpecs.add(methodBuilder.build());
        }

        // build view state class
        TypeName viewTypeName = TypeName.get(viewInterface.asType());
        TypeSpec.Builder viewStateSpecBuilder = TypeSpec.classBuilder(viewStateClassName)
                .superclass(ParameterizedTypeName.get(ClassName.get(ViewState.class), viewTypeName))
                .addSuperinterface(viewTypeName)
                .addAnnotation(Keep.class)
                .addAnnotation(Utils.getGeneratedAnnotationSpec())
                .addModifiers(Modifier.PUBLIC);

        // build constructor
        viewStateSpecBuilder.addMethod(
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Keep.class)
                        .addParameter(ClassName.get(annotatedElement), "presenter")
                        .addStatement("super(presenter)")
                        .build());

        for (StrategyMirror strategy : strategies) {
            String name = strategyRefName(strategy);
            Modifier[] modifiers = {Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL};
            if (strategy.type != null) {
                viewStateSpecBuilder.addField(FieldSpec.builder(strategy.type, name, modifiers)
                        .initializer("new $T()", strategy.type)
                        .build());
            } else if (strategy.typeMirror != null) {
                viewStateSpecBuilder.addField(FieldSpec.builder(TypeName.get(strategy.typeMirror), name, modifiers)
                        .initializer("new $T()", ClassName.get(strategy.typeMirror))
                        .build());
            }
        }

        // add view command types
        viewStateSpecBuilder.addTypes(commandTypeSpecs.values());

        // add view command methods
        viewStateSpecBuilder.addMethods(methodSpecs);

        TypeSpec viewStateSpec = viewStateSpecBuilder.build();

        // create file
        JavaFile javaFile = JavaFile.builder(getPackage(), viewStateSpec)
                .indent("    ")
                .build();

        // write file
        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return viewStateSpec;
    }

    private TypeSpec createCommandSpec(ExecutableElement method, String viewStateClassName) {
        String methodName = method.getSimpleName().toString();
        String className = makeCommandClassName(methodName);
        TypeName viewType = TypeName.get(viewInterface.asType());
        TypeSpec.Builder command = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Command.class), viewType));

        // add constructor and fields if method has parameters
        if (!method.getParameters().isEmpty()) {
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
            for (VariableElement var : method.getParameters()) {
                TypeName type = TypeName.get(var.asType());
                String name = var.getSimpleName().toString();
                command.addField(type, name, Modifier.FINAL);
                constructor.addParameter(type, name);
                constructor.addStatement("this.$L = $L", name, name);
            }
            command.addMethod(constructor.build());
        }

        MethodSpec.Builder call = MethodSpec.methodBuilder("call")
                .returns(TypeName.VOID)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(viewType, "view")
                        .addAnnotation(NonNull.class).build());

        // add logging if requested
        String methodArgs = methodArgs(method);
        if (annotatedElement.getAnnotation(GenerateViewState.class).logging()) {
            call.addStatement("$T.i($S, \"$L($L)\")",
                    ClassName.get("android.util", "Log"), viewStateClassName, methodName, methodArgs);
        }

        call.addStatement("view.$L($L)", methodName, methodArgs);
        command.addMethod(call.build());

        return command.build();
    }

    private String makeCommandClassName(String methodName) {
        return methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + "Command";
    }

    /**
     * Add method parameters from interface method.
     *
     * @param method        method
     * @param methodBuilder view state method builder
     * @return is successful
     */
    private boolean addMethodParameters(ExecutableElement method, MethodSpec.Builder methodBuilder) {
        for (VariableElement param : method.getParameters()) {
            String varName = param.getSimpleName().toString();
            if (FORBIDDEN_PARAMETER_NAMES.contains(varName)) {
                env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Parameter name must not be named '" +
                        varName + "'", param);
                return false;
            }
            // build parameter
            ParameterSpec.Builder parameter = ParameterSpec.builder(
                    TypeName.get(param.asType()), varName,
                    param.getModifiers().toArray(new Modifier[param.getModifiers().size()]));
            // add final modifier (solve issues with java6)
            if (!param.getModifiers().contains(Modifier.FINAL)) {
                parameter.addModifiers(Modifier.FINAL);
            }
            // copy parameter annotations
            for (AnnotationMirror annotationMirror : param.getAnnotationMirrors()) {
                parameter.addAnnotation(ClassName.get(
                        (TypeElement) env.getTypeUtils().asElement(annotationMirror.getAnnotationType())));
            }
            methodBuilder.addParameter(parameter.build());
        }
        return true;
    }

    /**
     * Get name for a reference to strategy.
     *
     * @param strategy strategy
     * @return camelCase reference name for the strategy field
     */
    private String strategyRefName(StrategyMirror strategy) {
        String name = strategy.getName();
        Pattern pattern = Pattern.compile("[A-Z][a-z0-9_]+");
        StringBuilder sb = new StringBuilder();
        if (name.length() == 1) {
            return name.toUpperCase();
        } else {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            return name.toUpperCase();
        }
        do {
            if (sb.length() > 0) {
                sb.append("_");
            }
            sb.append(matcher.group().toUpperCase());
        } while (matcher.find());
        return sb.toString();
    }

    /**
     * Resolve command tag.
     *
     * @param method method
     * @return tag, or null for default tag
     */
    private static String getCommandTag(ExecutableElement method) {
        Tag annotation = method.getAnnotation(Tag.class);
        if (annotation == null) {
            return null;
        }
        String value = annotation.value();
        return Tag.NO_TAG.equals(value) ? null : value;
    }

    @NonNull
    private String getPackage() {
        return env.getElementUtils().getPackageOf(annotatedElement).getQualifiedName().toString();
    }

    /**
     * Resolve strategy for command.
     *
     * @param method method
     * @return strategy type or null (if error occured)
     */
    private StrategyMirror resolveStrategy(ExecutableElement method) {
        CustomStrategy customStrategy = method.getAnnotation(CustomStrategy.class);
        Default defaultStrategy = method.getAnnotation(Default.class);
        Once onceStrategy = method.getAnnotation(Once.class);
        Single singleStrategy = method.getAnnotation(Single.class);
        if (!checkStrategyAnnotations(method, customStrategy, defaultStrategy, onceStrategy, singleStrategy)) {
            return null;
        }
        if (customStrategy != null) {
            return getCustomStrategy(method, customStrategy);
        }
        if (onceStrategy != null) {
            return new StrategyMirror(OnceStrategy.class);
        }
        if (singleStrategy != null) {
            return new StrategyMirror(SingleStrategy.class);
        }
        return new StrategyMirror(DefaultStrategy.class);
    }

    /**
     * Resolve custom strategy.
     *
     * @param method         view command
     * @param customStrategy annotation
     * @return strategy type, or null
     */
    private StrategyMirror getCustomStrategy(ExecutableElement method, CustomStrategy customStrategy) {
        TypeMirror strategyType = null;
        // hack to get custom annotation class
        try {
            customStrategy.value();
        } catch (MirroredTypeException e) {
            strategyType = e.getTypeMirror();
        }
        if (strategyType == null) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Cannot process @" + CustomStrategy.class.getSimpleName(), method);
            return null;
        }
        TypeElement strategyElement = (TypeElement) env.getTypeUtils().asElement(strategyType);
        if (!strategyElement.getModifiers().contains(Modifier.PUBLIC)) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format("%s must be public", strategyElement.getQualifiedName()),
                    strategyElement);
        }
        if (strategyElement.getModifiers().contains(Modifier.ABSTRACT)) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format("%s must not be abstract", strategyElement.getQualifiedName()),
                    strategyElement);
        }
        for (Element e : strategyElement.getEnclosedElements()) {
            if (e.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement) e;
            if (ee.getParameters().isEmpty()) {
                return new StrategyMirror(strategyType);
            }
        }
        env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                String.format("%s does not contain any empty public constructors", strategyElement.getQualifiedName()),
                strategyElement);
        return null;
    }

    /**
     * Check strategy annotation declared only once.
     *
     * @param method      checking method
     * @param annotations list of strategy annotations
     * @return was check successful or not
     */
    private boolean checkStrategyAnnotations(ExecutableElement method, Annotation... annotations) {
        Annotation first = null;
        for (Annotation annotation : annotations) {
            if (annotation != null) {
                if (first == null) {
                    first = annotation;
                } else {
                    env.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            String.format("%s cannot be used with %s; strategy must be declared exactly once",
                                    first, annotation),
                            method);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Generate method arguments for command.
     *
     * @param method executable element, e.g. method
     * @return method arguments, separated by comma
     */
    private static String methodArgs(ExecutableElement method) {
        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(parameters.get(0).getSimpleName());
        for (int i = 1, l = parameters.size(); i < l; i++) {
            sb.append(", ");
            sb.append(parameters.get(i).getSimpleName());
        }
        return sb.toString();
    }

    /**
     * Facade class for using provided {@link Strategy} implementations or custom user ones.
     */
    class StrategyMirror {
        final Class<? extends Strategy> type;
        final TypeMirror typeMirror;

        StrategyMirror(Class<? extends Strategy> type) {
            this.type = type;
            this.typeMirror = null;
        }

        StrategyMirror(TypeMirror typeMirror) {
            this.typeMirror = typeMirror;
            this.type = null;
        }

        /**
         * @return simple name of the type
         */
        String getName() {
            if (type != null) {
                return type.getSimpleName();
            }
            return env.getTypeUtils().asElement(typeMirror).getSimpleName().toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StrategyMirror)) {
                return false;
            }
            StrategyMirror another = (StrategyMirror) obj;
            if (type != null && another.type != null) {
                return type.equals(another.type);
            }
            if (typeMirror != null && another.typeMirror != null) {
                return typeMirror.equals(another.typeMirror);
            }
            return false;
        }

        @Override
        public int hashCode() {
            assert type != null || typeMirror != null;
            return type != null ? type.hashCode() : typeMirror.hashCode();
        }
    }

}
