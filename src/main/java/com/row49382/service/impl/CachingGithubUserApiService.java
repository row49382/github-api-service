package com.row49382.service.impl;

import com.row49382.config.CacheConfig;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.mapper.BiMapper;
import com.row49382.service.AbstractGithubUserApiService;
import com.row49382.service.JsonService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class CachingGithubUserApiService extends AbstractGithubUserApiService {

    public CachingGithubUserApiService(
            HttpClient client,
            JsonService jsonService,
            BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper,
            GitHubUserHttpClientRequestFactory requestFactory) {
        super(client, jsonService, responseMapper, requestFactory);
    }

    @Override
    @Cacheable(value = CacheConfig.GITHUB_USER_CACHE)
    public GithubUserAggregatedResponse fetchByUsername(String username) {
        GithubUserResponse userResponse = this.getGithubUser(username);
        List<GithubUserRepoResponse> userRepoResponses = this.getGithubRepos(username);

        return this.responseMapper.map(userResponse, userRepoResponses);
    }

    private GithubUserResponse getGithubUser(String username)
            throws JsonDeserializationException, GithubUserFetchException {
        HttpRequest request =
                this.requestFactory.buildRequest(GITHUB_USER_ENDPOINT_TEMPLATE.formatted(username));

        HttpResponse<String> response = this.tryClientCall(request);
        return this.jsonService.deserialize(response.body(), GithubUserResponse.class);
    }

    private List<GithubUserRepoResponse> getGithubRepos(String username)
            throws JsonDeserializationException, GithubUserFetchException {
        HttpRequest request =
                this.requestFactory.buildRequest(GITHUB_USER_REPO_ENDPOINT_TEMPLATE.formatted(username));

        HttpResponse<String> response = this.tryClientCall(request);
        return this.jsonService.deserializeList(response.body(), GithubUserRepoResponse.class);
    }
}
