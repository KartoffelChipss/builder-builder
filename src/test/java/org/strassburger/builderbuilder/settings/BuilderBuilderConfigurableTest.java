package org.strassburger.builderbuilder.settings;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import javax.swing.JComponent;

public class BuilderBuilderConfigurableTest extends BasePlatformTestCase {

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

    public void testCreateComponentReflectsCurrentSettings() {
        settings.methodPrefix = "set";
        settings.generateButMethod = true;
        settings.generateBuilderMethod = false;
        settings.generateNullSafety = true;

        BuilderBuilderConfigurable configurable = new BuilderBuilderConfigurable();
        JComponent component = configurable.createComponent();
        assertNotNull(component);

        assertFalse(configurable.isModified());
    }

    public void testApplyPersistsChangesToSettings() {
        BuilderBuilderConfigurable configurable = new BuilderBuilderConfigurable();
        configurable.createComponent();

        configurable.getMethodPrefixField().setText("set");
        configurable.getGenerateButMethodCheckBox().setSelected(true);
        configurable.getGenerateBuilderMethodCheckBox().setSelected(false);
        configurable.getGenerateNullSafetyCheckBox().setSelected(true);

        assertTrue(configurable.isModified());

        configurable.apply();

        assertEquals("set", settings.methodPrefix);
        assertTrue(settings.generateButMethod);
        assertFalse(settings.generateBuilderMethod);
        assertTrue(settings.generateNullSafety);
        assertFalse(configurable.isModified());
    }

    public void testResetRevertsUnappliedChanges() {
        settings.methodPrefix = "with";
        settings.generateBuilderMethod = true;

        BuilderBuilderConfigurable configurable = new BuilderBuilderConfigurable();
        configurable.createComponent();

        configurable.getMethodPrefixField().setText("unsaved");
        assertTrue(configurable.isModified());

        configurable.reset();

        assertFalse(configurable.isModified());
        assertEquals("with", configurable.getMethodPrefixField().getText());
    }
}
