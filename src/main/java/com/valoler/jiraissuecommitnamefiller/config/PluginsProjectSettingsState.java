package com.valoler.jiraissuecommitnamefiller.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "com.valoler.jiraissuecommitnamefiller.config.AppSettingsState",
        storages = @Storage("JiraIssueCommitMessageHeadFiller.xml")
)
@Data
public class PluginsProjectSettingsState implements PersistentStateComponent<PluginsProjectSettingsState> {

    private String userCredentials;
    private Long credentialsCount;

//    public static PluginsProjectSettingsState getInstance() {
//        return ApplicationManager.getApplication().getService(PluginsProjectSettingsState.class);
//    }

    public static PluginsProjectSettingsState getInstance(Project project) {
        return project.getService(PluginsProjectSettingsState.class);
    }

    @Override
    public @Nullable PluginsProjectSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PluginsProjectSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
