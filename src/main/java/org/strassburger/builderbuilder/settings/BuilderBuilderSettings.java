package org.strassburger.builderbuilder.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(name = "BuilderBuilderSettings", storages = @Storage("builder-builder.xml"))
public final class BuilderBuilderSettings implements PersistentStateComponent<BuilderBuilderSettings> {

    public String methodPrefix = "with";
    public boolean generateButMethod = false;
    public boolean generateBuilderMethod = true;
    public boolean generateNullSafety = false;

    public static BuilderBuilderSettings getInstance() {
        return ApplicationManager.getApplication().getService(BuilderBuilderSettings.class);
    }

    @Override
    public BuilderBuilderSettings getState() {
        return this;
    }

    @Override
    public void loadState(BuilderBuilderSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
