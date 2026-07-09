package org.strassburger.builderbuilder.ui;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.strassburger.builderbuilder.generator.BuilderGenerationOptions;

import java.util.Set;

public class GenerateBuilderDialogTest extends BasePlatformTestCase {

    public void testDefaultOptions() {
        GenerateBuilderDialog dialog = new GenerateBuilderDialog(getProject());
        try {
            assertEquals(new BuilderGenerationOptions("with", false, true, Set.of("name")), dialog.getOptions(Set.of("name")));
        } finally {
            dialog.close(0);
        }
    }

    public void testOptionsReflectFieldChanges() {
        GenerateBuilderDialog dialog = new GenerateBuilderDialog(getProject());
        try {
            dialog.getMethodPrefixField().setText("set");
            dialog.getGenerateButMethodCheckBox().setSelected(true);
            dialog.getGenerateBuilderMethodCheckBox().setSelected(false);

            assertEquals(new BuilderGenerationOptions("set", true, false, Set.of("name")), dialog.getOptions(Set.of("name")));
        } finally {
            dialog.close(0);
        }
    }
}
