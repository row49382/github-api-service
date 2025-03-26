package com.row49382.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;

@Component
public class GitHubUserHttpClientRequestFactory {
    @Value("${github-api-url}")
    private String githubUrl;

    @Value("${github-user-agent}")
    private String githubUserAgent;

    public HttpRequest buildRequest(String endpoint) {
        return HttpRequest.newBuilder()
                    .uri(URI.create(githubUrl + endpoint))
                    .header("User-Agent", githubUserAgent)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-Github-Api-Version", "2022-11-28")
                    .GET()
                    .build();
    }
}
