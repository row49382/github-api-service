package com.row49382.service;

import com.row49382.exception.GithubUserFetchException;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.scheduling.annotation.Async;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractAsyncRESTHandler<T> {
    protected final HttpClient client;
    protected final GitHubUserHttpClientRequestFactory requestFactory;
    protected final JsonService jsonService;

    protected AbstractAsyncRESTHandler(
            HttpClient client,
            GitHubUserHttpClientRequestFactory requestFactory,
            JsonService jsonService) {
        this.client = client;
        this.requestFactory = requestFactory;
        this.jsonService = jsonService;
    }

    @Async
    public CompletableFuture<T> handle(String endpoint) {
        HttpRequest request = this.requestFactory.buildRequest(endpoint);
        CompletableFuture<HttpResponse<String>> response =
                this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return response.thenApply(res -> {
            this.verifySuccessfulResponse(res);
            return this.deserializeResponse(res.body());
        });
    }

    protected abstract T deserializeResponse(String body);

    protected void verifySuccessfulResponse(HttpResponse<?> response) {
        if (response.statusCode() != 200) {
            throw new GithubUserFetchException(response.uri().toString());
        }
    }
}
