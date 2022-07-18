package com.valoler.jiraissuecommitnamefiller.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.valoler.jiraissuecommitnamefiller.utils.AppSettingsUtils;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "com.valoler.jiraissuecommitnamefiller.config.AppSettingsState",
        storages = @Storage("JiraIssueCommitMessageHeadFiller.xml")
)
@Data
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    private String userCredentials;
    private Long credentialsCount;

    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    @Override
    public @Nullable AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
