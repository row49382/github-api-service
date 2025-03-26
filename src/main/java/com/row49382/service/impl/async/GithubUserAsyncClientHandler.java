package com.row49382.service.impl.async;

import com.row49382.service.AsyncHandler;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@Component
public class GithubUserAsyncClientHandler implements AsyncHandler<String> {
    private final HttpClient client;
    private final GitHubUserHttpClientRequestFactory requestFactory;

    public GithubUserAsyncClientHandler(
            HttpClient client,
            GitHubUserHttpClientRequestFactory requestFactory) {
        this.client = client;
        this.requestFactory = requestFactory;
    }

    @Override
    @Async
    public CompletableFuture<HttpResponse<String>> handle(String endpoint) {
        HttpRequest request = this.requestFactory.buildRequest(endpoint);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
