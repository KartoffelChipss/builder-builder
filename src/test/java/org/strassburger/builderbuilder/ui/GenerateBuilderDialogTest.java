package org.strassburger.builderbuilder.ui;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.strassburger.builderbuilder.generator.BuilderGenerationOptions;
import org.strassburger.builderbuilder.settings.BuilderBuilderSettings;

import java.util.Set;

public class GenerateBuilderDialogTest extends BasePlatformTestCase {

    private BuilderBuilderSettings settings;
    private String originalPrefix;
    private boolean originalButMethod;
    private boolean originalBuilderMethod;
    private boolean originalNullSafety;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        settings = BuilderBuilderSettings.getInstance();
        originalPrefix = settings.methodPrefix;
        originalButMethod = settings.generateButMethod;
        originalBuilderMethod = settings.generateBuilderMethod;
        originalNullSafety = settings.generateNullSafety;
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            settings.methodPrefix = originalPrefix;
            settings.generateButMethod = originalButMethod;
            settings.generateBuilderMethod = originalBuilderMethod;
            settings.generateNullSafety = originalNullSafety;
        } finally {
            super.tearDown();
        }
    }

    public void testInitialStateComesFromApplicationSettings() {
        settings.methodPrefix = "set";
        settings.generateButMethod = true;
        settings.generateBuilderMethod = false;
        settings.generateNullSafety = true;

        GenerateBuilderDialog dialog = new GenerateBuilderDialog(getProject());
        try {
            assertEquals(new BuilderGenerationOptions("set", true, false, true, Set.of("name")), dialog.getOptions(Set.of("name")));
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
            dialog.getGenerateNullSafetyCheckBox().setSelected(true);

            assertEquals(new BuilderGenerationOptions("set", true, false, true, Set.of("name")), dialog.getOptions(Set.of("name")));
        } finally {
            dialog.close(0);
        }
    }
}
