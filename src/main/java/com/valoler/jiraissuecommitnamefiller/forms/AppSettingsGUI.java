package com.valoler.jiraissuecommitnamefiller.forms;

import com.intellij.openapi.ui.Messages;
import com.valoler.jiraissuecommitnamefiller.entity.JiraAuthInfoResponse;
import com.valoler.jiraissuecommitnamefiller.integration.JiraClient;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ConnectException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@Data
public class AppSettingsGUI {
    private JPanel rootPanel;
    private JPasswordField userPasswordField;
    private JTextField jiraURLField;
    private JTextField userLoginField;
    private JPanel dataPanel;
    private JButton testConnectionButton;

    private static final int LOGIN_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;
    private static final int URL_INDEX = 2;

    Map<Integer, JTextField> userCredentialsComponents = new HashMap<>(
            Map.of(
                    LOGIN_INDEX, userLoginField,
                    PASSWORD_INDEX, userPasswordField,
                    URL_INDEX, jiraURLField
            )
    );

    public AppSettingsGUI() {
        testConnectionButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        testConnection();
                    }
                });
    }

    public JComponent getPreferredFocusedComponent() {
        return userLoginField;
    }

    private void testConnection() {

        if(StringUtils.isBlank(jiraURLField.getText())
                || StringUtils.isBlank(userLoginField.getText())
                || userPasswordField.getPassword().length <= 0) {
            Messages.showInfoMessage("Please, insert data to all fields.", "Empty Fields Message");
            return;
        }

        JiraClient jiraClient = new JiraClient(jiraURLField.getText(),
                userLoginField.getText().toCharArray(),
                userPasswordField.getPassword()
        );

        try {
            HttpResponse<Supplier<JiraAuthInfoResponse>> response = jiraClient.sendAsyncAuthRequest().get();

            int statusCode = response.statusCode();

            if (200 == statusCode) {
                String userFoundMessageTemplate = "Welcome %s!";
                Messages.showInfoMessage(
                        String.format(userFoundMessageTemplate, response.body().get().getName()),
                        "Connection Test Result"
                );
            } else {
                String messageTemplate = "Incorrect login or password";
                Messages.showInfoMessage(messageTemplate, "Connection Test Result");
            }
        } catch (InterruptedException e) {
            Messages.showErrorDialog(e.getMessage(), "Error");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e){
            String errorMessageTemplate;
            if(e.getCause() instanceof ConnectException) {
                errorMessageTemplate = "Connection to [%s] is not possible. Check your internet connection and VPN.";
                Messages.showErrorDialog(
                        String.format(errorMessageTemplate, jiraURLField.getText()),
                        "Connection Error");
            } else {
                errorMessageTemplate = "An error occurred: [%s]";
                Messages.showErrorDialog(String.format(errorMessageTemplate,e.getCause()), "Execution Error");
            }
        }
    }
}
