package org.strassburger.builderbuilder.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class BuilderGenerator {

    private static final String BUILDER_CLASS_NAME = "Builder";
    private static final String BUT_METHOD_NAME = "but";
    private static final String BUILDER_FACTORY_METHOD_NAME = "builder";

    private BuilderGenerator() {
    }

    public static boolean canGenerate(PsiClass psiClass) {
        return psiClass.findInnerClassByName(BUILDER_CLASS_NAME, false) == null;
    }

    public static void generate(Project project, PsiClass psiClass, BuilderGenerationOptions options) {
        PsiField[] instanceFields = instanceFields(psiClass);
        PsiMethod allArgsConstructor = findAllArgsConstructor(psiClass, instanceFields);
        PsiField[] fields = allArgsConstructor != null ? instanceFields : nonFinalFields(instanceFields);

        StringBuilder text = new StringBuilder();
        if (options.generateBuilderMethod()) {
            text.append(builderFactoryMethodText());
        }
        text.append(builderClassText(psiClass.getName(), fields, allArgsConstructor != null, options));

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiClass dummyContainer = factory.createClassFromText(text.toString(), psiClass);

        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);
        CodeStyleManager formatter = CodeStyleManager.getInstance(project);

        PsiElement addedBuilder = psiClass.add(dummyContainer.getInnerClasses()[0]);
        codeStyleManager.shortenClassReferences(addedBuilder);
        formatter.reformat(addedBuilder);

        if (options.generateBuilderMethod()) {
            PsiElement addedFactoryMethod = psiClass.addBefore(dummyContainer.getMethods()[0], addedBuilder);
            codeStyleManager.shortenClassReferences(addedFactoryMethod);
            formatter.reformat(addedFactoryMethod);
        }
    }

    private static PsiField[] instanceFields(PsiClass psiClass) {
        return Arrays.stream(psiClass.getFields())
                .filter(field -> !field.hasModifierProperty(PsiModifier.STATIC))
                .toArray(PsiField[]::new);
    }

    private static PsiField[] nonFinalFields(PsiField[] fields) {
        return Arrays.stream(fields)
                .filter(field -> !field.hasModifierProperty(PsiModifier.FINAL))
                .toArray(PsiField[]::new);
    }

    /**
     * Finds a constructor whose parameters match the given fields 1:1, in order and by type,
     * so the builder can construct immutable instances through it instead of field assignment.
     */
    @Nullable
    private static PsiMethod findAllArgsConstructor(PsiClass psiClass, PsiField[] fields) {
        for (PsiMethod constructor : psiClass.getConstructors()) {
            PsiParameter[] parameters = constructor.getParameterList().getParameters();
            if (parameters.length != fields.length) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; i < parameters.length; i++) {
                if (!parameters[i].getType().getCanonicalText().equals(fields[i].getType().getCanonicalText())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return constructor;
            }
        }
        return null;
    }

    private static String builderClassText(String className, PsiField[] fields, boolean useConstructor, BuilderGenerationOptions options) {
        String methodPrefix = options.methodPrefix();
        StringBuilder text = new StringBuilder();
        text.append("public static class ").append(BUILDER_CLASS_NAME).append(" {\n");

        for (PsiField field : fields) {
            text.append("private ").append(field.getType().getCanonicalText()).append(' ').append(field.getName()).append(";\n");
        }

        for (PsiField field : fields) {
            String type = field.getType().getCanonicalText();
            String name = field.getName();
            text.append("public ").append(BUILDER_CLASS_NAME).append(' ').append(methodName(methodPrefix, name))
                    .append('(').append(type).append(' ').append(name).append(") {\n")
                    .append("this.").append(name).append(" = ").append(name).append(";\n")
                    .append("return this;\n")
                    .append("}\n");
        }

        if (options.generateButMethod()) {
            text.append(butMethodText(fields, methodPrefix));
        }

        text.append("public ").append(className).append(" build() {\n");
        if (useConstructor) {
            text.append("return new ").append(className).append('(');
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) {
                    text.append(", ");
                }
                text.append(fields[i].getName());
            }
            text.append(");\n");
        } else {
            text.append(className).append(" result = new ").append(className).append("();\n");
            for (PsiField field : fields) {
                String name = field.getName();
                text.append("result.").append(name).append(" = this.").append(name).append(";\n");
            }
            text.append("return result;\n");
        }
        text.append("}\n");

        text.append("}\n");
        return text.toString();
    }

    private static String butMethodText(PsiField[] fields, String methodPrefix) {
        StringBuilder text = new StringBuilder();
        text.append("public ").append(BUILDER_CLASS_NAME).append(' ').append(BUT_METHOD_NAME).append("() {\n")
                .append("return new ").append(BUILDER_CLASS_NAME).append("()");
        for (PsiField field : fields) {
            String name = field.getName();
            text.append('.').append(methodName(methodPrefix, name)).append('(').append(name).append(')');
        }
        text.append(";\n}\n");
        return text.toString();
    }

    private static String builderFactoryMethodText() {
        return "public static " + BUILDER_CLASS_NAME + " " + BUILDER_FACTORY_METHOD_NAME + "() {\n"
                + "return new " + BUILDER_CLASS_NAME + "();\n"
                + "}\n";
    }

    private static String methodName(String prefix, String fieldName) {
        if (prefix.isEmpty()) {
            return fieldName;
        }
        return prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}
