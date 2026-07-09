package org.strassburger.builderbuilder.generator;

import java.util.Set;

public record BuilderGenerationOptions(
        String methodPrefix,
        boolean generateButMethod,
        boolean generateBuilderMethod,
        boolean generateNullSafety,
        Set<String> selectedFieldNames) {
}
