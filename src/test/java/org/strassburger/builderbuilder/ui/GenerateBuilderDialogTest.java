package org.strassburger.builderbuilder.ui;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.strassburger.builderbuilder.generator.BuilderGenerationOptions;

public class GenerateBuilderDialogTest extends BasePlatformTestCase {

    public void testDefaultOptions() {
        GenerateBuilderDialog dialog = new GenerateBuilderDialog(getProject());
        try {
            assertEquals(new BuilderGenerationOptions("with", false, true), dialog.getOptions());
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

            assertEquals(new BuilderGenerationOptions("set", true, false), dialog.getOptions());
        } finally {
            dialog.close(0);
        }
    }
}
