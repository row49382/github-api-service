package com.row49382.service.impl;

import com.row49382.config.CacheConfig;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.mapper.BiMapper;
import com.row49382.service.GithubUserApiService;
import com.row49382.service.JsonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class CachingGithubUserApiService implements GithubUserApiService {
    private static final String GITHUB_USER_ENDPOINT_TEMPLATE = "/users/%s";
    private static final String GITHUB_USER_REPO_ENDPOINT_TEMPLATE = GITHUB_USER_ENDPOINT_TEMPLATE + "/repos";
    private static final String GITHUB_USER_FETCH_ERROR_TEMPLATE = "Failed to fetch user information with request %s";

    private final HttpClient client;
    private final JsonService jsonService;
    private final BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper;

    @Value("${github-api-url}")
    private String githubUrl;

    @Value("${github-user-agent}")
    private String githubUserAgent;

    public CachingGithubUserApiService(
            HttpClient client,
            JsonService jsonService,
            BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper) {
        this.client = client;
        this.jsonService = jsonService;
        this.responseMapper = responseMapper;
    }

    @Override
    @Cacheable(value = CacheConfig.GITHUB_USER_CACHE)
    public GithubUserAggregatedResponse fetchByUsername(String username)
            throws JsonDeserializationException, GithubUserFetchException {
        GithubUserResponse userResponse = this.getGithubUser(username);
        List<GithubUserRepoResponse> userRepoResponses = this.getGithubRepos(username);

        return this.responseMapper.map(userResponse, userRepoResponses);
    }

    private GithubUserResponse getGithubUser(String username)
            throws JsonDeserializationException, GithubUserFetchException {
        HttpRequest request =
                this.buildRequest(GITHUB_USER_ENDPOINT_TEMPLATE.formatted(username));

        HttpResponse<String> response = this.tryClientCall(request);
        return this.jsonService.deserialize(response.body(), GithubUserResponse.class);
    }

    private List<GithubUserRepoResponse> getGithubRepos(String username)
            throws JsonDeserializationException, GithubUserFetchException {
        HttpRequest request =
                this.buildRequest(GITHUB_USER_REPO_ENDPOINT_TEMPLATE.formatted(username));

        HttpResponse<String> response = this.tryClientCall(request);
        return this.jsonService.deserializeList(response.body(), GithubUserRepoResponse.class);
    }

    private HttpResponse<String> tryClientCall(HttpRequest request) throws GithubUserFetchException {
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

    HttpRequest buildRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(this.githubUrl + endpoint))
                .header("User-Agent", this.githubUserAgent)
                .header("Accept", "application/vnd.github+json")
                .header("X-Github-Api-Version", "2022-11-28")
                .GET()
                .build();
    }
}
