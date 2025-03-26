package com.row49382.service.impl.async;

import com.row49382.config.CacheConfig;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.mapper.BiMapper;
import com.row49382.service.AbstractGithubUserApiService;
import com.row49382.service.AsyncHandler;
import com.row49382.service.JsonService;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class AsyncCachingGithubUserService extends AbstractGithubUserApiService {
    private final AsyncHandler<String> asyncHandler;

    public AsyncCachingGithubUserService(
            HttpClient client,
            JsonService jsonService,
            BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper,
            GitHubUserHttpClientRequestFactory requestFactory,
            AsyncHandler<String> asyncHandler) {
        super(client, jsonService, responseMapper, requestFactory);
        this.asyncHandler = asyncHandler;
    }

    @Override
    @Cacheable(value = CacheConfig.GITHUB_USER_CACHE)
    public GithubUserAggregatedResponse fetchByUsername(String username) throws Throwable {
        CompletableFuture<HttpResponse<String>> userResponse =
                this.asyncHandler.handle(GITHUB_USER_ENDPOINT_TEMPLATE.formatted(username));
        CompletableFuture<HttpResponse<String>> userRepoResponses =
                this.asyncHandler.handle(GITHUB_USER_REPO_ENDPOINT_TEMPLATE.formatted(username));

        try {
            return userResponse
                    .thenCompose(ur -> userRepoResponses
                            .thenApply(urr -> {
                                this.verifySuccessfulResponse(ur);
                                this.verifySuccessfulResponse(urr);

                                GithubUserResponse githubUserResponse =
                                        this.jsonService.deserialize(ur.body(), GithubUserResponse.class);
                                List<GithubUserRepoResponse> githubUserRepoResponse =
                                        this.jsonService.deserializeList(urr.body(), GithubUserRepoResponse.class);

                                return this.responseMapper.map(githubUserResponse, githubUserRepoResponse);
                            })).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GithubUserFetchException("failed to resolve async calls in time", e);
        } catch (ExecutionException e) {
            if (e.getCause() != null) {
                throw new Exception(e.getCause());
            }

            throw e;
        }
    }

    private <T> void verifySuccessfulResponse(HttpResponse<T> response) {
        if (response.statusCode() != 200) {
            throw new GithubUserFetchException(GITHUB_USER_FETCH_ERROR_TEMPLATE.formatted(response.body()));
        }
    }
}
