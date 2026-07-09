package org.strassburger.builderbuilder.settings;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class BuilderBuilderSettingsTest extends BasePlatformTestCase {

    public void testDefaults() {
        BuilderBuilderSettings settings = new BuilderBuilderSettings();

        assertEquals("with", settings.methodPrefix);
        assertFalse(settings.generateButMethod);
        assertTrue(settings.generateBuilderMethod);
        assertFalse(settings.generateNullSafety);
    }

    public void testGetInstanceReturnsSharedApplicationService() {
        BuilderBuilderSettings first = BuilderBuilderSettings.getInstance();
        BuilderBuilderSettings second = BuilderBuilderSettings.getInstance();

        assertSame(first, second);
    }

    public void testLoadStateCopiesValues() {
        BuilderBuilderSettings settings = new BuilderBuilderSettings();
        BuilderBuilderSettings loaded = new BuilderBuilderSettings();
        loaded.methodPrefix = "set";
        loaded.generateButMethod = true;
        loaded.generateBuilderMethod = false;
        loaded.generateNullSafety = true;

        settings.loadState(loaded);

        assertEquals("set", settings.methodPrefix);
        assertTrue(settings.generateButMethod);
        assertFalse(settings.generateBuilderMethod);
        assertTrue(settings.generateNullSafety);
        assertSame(settings, settings.getState());
    }
}
