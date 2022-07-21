package com.valoler.jiraissuecommitnamefiller.integration;

import com.valoler.jiraissuecommitnamefiller.entity.JiraAuthInfoResponse;
import com.valoler.jiraissuecommitnamefiller.entity.JiraIssueResponse;
import com.valoler.jiraissuecommitnamefiller.exception.UrlIsNotValidException;
import com.valoler.jiraissuecommitnamefiller.handler.JsonBodyHandler;
import com.valoler.jiraissuecommitnamefiller.utils.PluginsProjectSettingsUtils;
import org.apache.commons.validator.routines.UrlValidator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class JiraClient {

    private final HttpClient httpClient;
    private final String jiraUrl;
    private final String basicAuthHeader;
    private final UrlValidator urlValidator;

    public JiraClient(String jiraUrl, char[] login, char[] password) {
        this.httpClient = HttpClient.newHttpClient();
        this.jiraUrl = jiraUrl.endsWith("/") ? jiraUrl : jiraUrl.concat("/");
        this.basicAuthHeader = PluginsProjectSettingsUtils.getBasicAuthenticationHeader(login, password);
        this.urlValidator = new UrlValidator();
    }


    public CompletableFuture<HttpResponse<Supplier<JiraAuthInfoResponse>>> sendAsyncAuthRequest()
            throws UrlIsNotValidException {

        String url = "rest/auth/1/session";

        String checkUserUrl = jiraUrl.concat(url);

        if(urlValidator.isValid(jiraUrl)) {
            var request = HttpRequest.newBuilder(
                                             URI.create(checkUserUrl)
                                     )
                                     .timeout(Duration.of(1000, ChronoUnit.MILLIS))
                                     .header("accept", "application/json")
                                     .header("Authorization", basicAuthHeader)
                                     .build();
            return httpClient.sendAsync(request, new JsonBodyHandler<>(JiraAuthInfoResponse.class));
        } else {
            throw new UrlIsNotValidException(jiraUrl);
        }
    }


    public CompletableFuture<HttpResponse<Supplier<JiraIssueResponse>>> sendIssueInfoRequest(String issueCode)
            throws UrlIsNotValidException {

        String urlTemplate = "rest/api/2/issue/%s?fields=summary";

        String url = jiraUrl.concat(String.format(urlTemplate, issueCode));
        if(urlValidator.isValid(jiraUrl)) {
            var request = HttpRequest.newBuilder(
                                             URI.create(url)
                                     )
                                     .timeout(Duration.of(1000, ChronoUnit.MILLIS))
                                     .header("accept", "application/json")
                                     .header("Authorization", basicAuthHeader)
                                     .build();

            return httpClient.sendAsync(request, new JsonBodyHandler<>(JiraIssueResponse.class));
        } else {
            throw new UrlIsNotValidException(jiraUrl);
        }
    }
}
