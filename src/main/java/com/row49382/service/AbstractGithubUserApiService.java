package com.row49382.service;

import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.mapper.BiMapper;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public abstract class AbstractGithubUserApiService implements GithubUserApiService {
    protected static final String GITHUB_USER_ENDPOINT_TEMPLATE = "/users/%s";
    protected static final String GITHUB_USER_REPO_ENDPOINT_TEMPLATE = GITHUB_USER_ENDPOINT_TEMPLATE + "/repos";
    protected static final String GITHUB_USER_FETCH_ERROR_TEMPLATE = "Failed to fetch user information with request %s";

    protected final HttpClient client;
    protected final JsonService jsonService;
    protected final BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper;
    protected final GitHubUserHttpClientRequestFactory requestFactory;

    protected AbstractGithubUserApiService(
            HttpClient client,
            JsonService jsonService,
            BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper,
            GitHubUserHttpClientRequestFactory requestFactory) {
        this.client = client;
        this.jsonService = jsonService;
        this.responseMapper = responseMapper;
        this.requestFactory = requestFactory;
    }

    protected HttpResponse<String> tryClientCall(HttpRequest request) {
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new GithubUserFetchException(GITHUB_USER_FETCH_ERROR_TEMPLATE.formatted(request));
            }

            return response;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new GithubUserFetchException(GITHUB_USER_FETCH_ERROR_TEMPLATE.formatted(request), ie);
        } catch (IOException ioe) {
            throw new GithubUserFetchException(GITHUB_USER_FETCH_ERROR_TEMPLATE.formatted(request), ioe);
        }
    }
}
