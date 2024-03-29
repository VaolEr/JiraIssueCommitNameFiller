package com.valoler.jiraissuecommitnamefiller.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.valoler.jiraissuecommitnamefiller.forms.PluginsProjectSettingsGUI;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.valoler.jiraissuecommitnamefiller.utils.PluginsProjectSettingsUtils.decodeString;
import static com.valoler.jiraissuecommitnamefiller.utils.PluginsProjectSettingsUtils.decodeUserCredentials;
import static com.valoler.jiraissuecommitnamefiller.utils.PluginsProjectSettingsUtils.encodeUserCredentials;
import static java.util.Objects.isNull;

public class PluginsProjectSettingsConfigurable implements Configurable {

    private PluginsProjectSettingsGUI settingsGUI;

    private final Project project;

    private final PluginsProjectSettingsState settings;

    public PluginsProjectSettingsConfigurable(Project project) {
        this.project = project;
        this.settings = PluginsProjectSettingsState.getInstance(project);
    }

    @Override
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getDisplayName() {
        return "Jira Issue Commit Name Filler Plugin";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsGUI.getPreferredFocusedComponent();
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsGUI = new PluginsProjectSettingsGUI();
        return settingsGUI.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return !StringUtils.equals(
                settings.getUserCredentials(),
                encodeUserCredentials(project, getUserCredentialsAsCharArrays())
        );
    }

    @Override
    public void apply() throws ConfigurationException {
        settings.setUserCredentials(encodeUserCredentials(project, getUserCredentialsAsCharArrays()));
    }

    @Override
    public void reset() {
        if (isNull(settings.getUserCredentials())
                || decodeUserCredentials(settings.getUserCredentials()).length <= 0) {
            return;
        }
        settingsGUI.getUserCredentialsComponents()
                   .forEach((key, value) -> value.setText(
                                   decodeString(
                                           decodeUserCredentials(
                                                   settings.getUserCredentials()
                                           )[key]
                                   )
                           )
                   );
    }

    @Override
    public void disposeUIResources() {
        settingsGUI = null;
    }

    @NotNull
    private List<char[]> getUserCredentialsAsCharArrays() {
        return settingsGUI.getUserCredentialsComponents()
                          .values()
                          .stream()
                          .map(field ->
                                  field instanceof JPasswordField
                                          ? ((JPasswordField) field).getPassword()
                                          : field.getText().toCharArray()
                          )
                          .collect(Collectors.toList());
    }
}
