package com.valoler.jiraissuecommitnamefiller.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import com.valoler.jiraissuecommitnamefiller.config.AppSettingsState;
import com.valoler.jiraissuecommitnamefiller.entity.JiraIssueResponse;
import com.valoler.jiraissuecommitnamefiller.integration.JiraClient;
import com.valoler.jiraissuecommitnamefiller.utils.AppSettingsUtils;
import git4idea.branch.GitBranchUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.net.ConnectException;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class CommitMessageHeadFIllerAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        AppSettingsState settings = AppSettingsState.getInstance();
        String[] credentials = AppSettingsUtils
                .decodeUserCredentials(settings.getUserCredentials());

        if (credentials.length <= 0) {
            Messages.showInfoMessage("Please, fill user credentials in plugin settings. \n" +
                            "File -> Settings -> Tools -> Commit Message Header Filler Plugin or" +
                            " CTRL + ALT + S -> Tools -> Commit Message Header Filler Plugin",
                    "Plugin Settings Message");
            return;
        }

        JiraClient jiraClient = new JiraClient(
                AppSettingsUtils.decodeString(credentials[2]),
                AppSettingsUtils.decodeString(credentials[0]).toCharArray(),
                AppSettingsUtils.decodeString(credentials[1]).toCharArray()
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
                        String.format(errorMessageTemplate, AppSettingsUtils.decodeString(credentials[2])),
                        "Connection Error");
            } else {
                errorMessageTemplate = "An error occurred: [%s]";
                Messages.showErrorDialog(String.format(errorMessageTemplate, e.getCause()), "Execution Error");
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
