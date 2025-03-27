package com.row49382.service.impl.async;

import com.row49382.config.CacheConfig;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.mapper.BiMapper;
import com.row49382.service.AbstractGithubUserApiService;
import com.row49382.service.AsyncRESTHandler;
import com.row49382.service.JsonService;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class AsyncCachingGithubUserService extends AbstractGithubUserApiService {
    private final AsyncRESTHandler<GithubUserResponse> asyncGithubUserHandler;
    private final AsyncRESTHandler<List<GithubUserRepoResponse>> asyncGithubUserRepoHandler;

    public AsyncCachingGithubUserService(
            HttpClient client,
            JsonService jsonService,
            BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper,
            GitHubUserHttpClientRequestFactory requestFactory,
            AsyncRESTHandler<GithubUserResponse> asyncGithubUserHandler,
            AsyncRESTHandler<List<GithubUserRepoResponse>> asyncGithubUserRepoHandler) {
        super(client, jsonService, responseMapper, requestFactory);
        this.asyncGithubUserHandler = asyncGithubUserHandler;
        this.asyncGithubUserRepoHandler = asyncGithubUserRepoHandler;
    }

    @Override
    @Cacheable(value = CacheConfig.GITHUB_USER_CACHE)
    public GithubUserAggregatedResponse fetchByUsername(String username) throws Throwable {
        CompletableFuture<GithubUserResponse> userResponse =
                this.asyncGithubUserHandler.handle(GITHUB_USER_ENDPOINT_TEMPLATE.formatted(username));
        CompletableFuture<List<GithubUserRepoResponse>> userRepoResponses =
                this.asyncGithubUserRepoHandler.handle(GITHUB_USER_REPO_ENDPOINT_TEMPLATE.formatted(username));

        try {
            return userResponse.thenCombine(userRepoResponses, this.responseMapper::map).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GithubUserFetchException("failed to resolve async calls in time", e);
        } catch (ExecutionException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }

            throw e;
        }
    }
}
