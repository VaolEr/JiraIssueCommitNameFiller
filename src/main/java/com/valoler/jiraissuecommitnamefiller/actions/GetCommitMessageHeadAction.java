package com.valoler.jiraissuecommitnamefiller.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import com.valoler.jiraissuecommitnamefiller.config.PluginsProjectSettingsState;
import com.valoler.jiraissuecommitnamefiller.entity.JiraIssueResponse;
import com.valoler.jiraissuecommitnamefiller.exception.UrlIsNotValidException;
import com.valoler.jiraissuecommitnamefiller.integration.JiraClient;
import com.valoler.jiraissuecommitnamefiller.utils.PluginsProjectSettingsUtils;
import git4idea.branch.GitBranchUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.net.ConnectException;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class GetCommitMessageHeadAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        PluginsProjectSettingsState settings = PluginsProjectSettingsState.getInstance(
                Objects.requireNonNull(event.getProject())
        );

        String encodedCredentials = settings.getUserCredentials();

        String[] credentials = PluginsProjectSettingsUtils.decodeUserCredentials(encodedCredentials);

        if (StringUtils.isBlank(encodedCredentials) || credentials.length <= 0) {
            Messages.showInfoMessage("Please, fill user credentials in plugin settings and press Apply. \n\n" +
                            "File -> Settings -> Tools -> Commit Message Header Filler Plugin\n\n or\n\n " +
                            "CTRL + ALT + S -> Tools -> Commit Message Header Filler Plugin",
                    "Plugin Settings Message");
            return;
        }

        JiraClient jiraClient = new JiraClient(
                PluginsProjectSettingsUtils.decodeString(credentials[2]),
                PluginsProjectSettingsUtils.decodeString(credentials[0]).toCharArray(),
                PluginsProjectSettingsUtils.decodeString(credentials[1]).toCharArray()
        );

        try {
            String[] splitBranchName = Objects.requireNonNull(
                                            Objects.requireNonNull(
                                                    GitBranchUtil.getCurrentRepository(
                                                            Objects.requireNonNull(event.getProject())
                                                    )
                                            ).getCurrentBranch())
                                    .getName().split("/");
            @NotNull String currentBranch = splitBranchName[splitBranchName.length-1];

            HttpResponse<Supplier<JiraIssueResponse>> response = jiraClient.sendIssueInfoRequest(currentBranch).get();

            int statusCode = response.statusCode();

            if (200 == statusCode) {
                CommitMessageI commitPanel = getCommitPanel(event);
                if (commitPanel == null) return;
                String commitHeaderTemplate = "%s: %s;\n\n%s";
                String commitMessage = parseExistingCommitMessage(commitPanel);
                String issueSummary = response.body().get().getJiraIssueFields().getSummary();
                commitPanel.setCommitMessage(String.format(commitHeaderTemplate, currentBranch, issueSummary, commitMessage));
            } else {
                String messageTemplate = "Issue with code %s not found.";
                Messages.showInfoMessage(
                        String.format(messageTemplate, currentBranch),
                        "Info"
                );
            }
        } catch (InterruptedException e) {
            Messages.showErrorDialog(e.getMessage(), "Error");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            String errorMessageTemplate;
            if (e.getCause() instanceof ConnectException) {
                errorMessageTemplate = "Connection to [%s] is not possible. Check your internet connection and VPN.";
                Messages.showErrorDialog(
                        String.format(errorMessageTemplate, PluginsProjectSettingsUtils.decodeString(credentials[2])),
                        "Connection Error");
            } else {
                errorMessageTemplate = "An error occurred: [%s]";
                String errorMsg;
                if (e instanceof UrlIsNotValidException){
                    errorMsg = String.format(errorMessageTemplate, "URL format is not correct.");
                } else {
                    errorMsg = String.format(errorMessageTemplate, e.getCause());
                }
                Messages.showErrorDialog(errorMsg, "Execution Error");
            }
        }
    }

    private String parseExistingCommitMessage(CommitMessageI commitPanel) {
        if (commitPanel instanceof CheckinProjectPanel) {
            return ((CheckinProjectPanel) commitPanel).getCommitMessage();
        }
        return null;
    }

    @Nullable
    private static CommitMessageI getCommitPanel(@Nullable AnActionEvent event) {
        if (event == null) {
            return null;
        }
        Refreshable data = Refreshable.PANEL_KEY.getData(event.getDataContext());
        if (data instanceof CommitMessageI) {
            return (CommitMessageI) data;
        }
        return VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(event.getDataContext());
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
