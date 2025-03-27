package com.row49382.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.mapper.BiMapper;
import com.row49382.service.AbstractAsyncRESTHandler;
import com.row49382.service.GithubUserApiService;
import com.row49382.service.JsonService;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import com.row49382.service.impl.async.AsyncCachingGithubUserService;
import com.row49382.service.impl.CachingGithubUserApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.net.http.HttpClient;
import java.util.List;

@Configuration
public class BeanConfig {
    @Value("${handle-async:false}")
    private boolean handleAsync;

    private final JsonService jsonService;
    private final BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper;
    private final AbstractAsyncRESTHandler<GithubUserResponse> asyncGithubUserHandler;
    private final AbstractAsyncRESTHandler<List<GithubUserRepoResponse>> asyncGithubUserRepoHandler;
    private final GitHubUserHttpClientRequestFactory requestFactory;

    public BeanConfig(
            @Lazy JsonService jsonService,
            @Lazy BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper,
            @Lazy AbstractAsyncRESTHandler<GithubUserResponse> asyncGithubUserHandler,
            @Lazy AbstractAsyncRESTHandler<List<GithubUserRepoResponse>> asyncGithubUserRepoHandler,
            @Lazy GitHubUserHttpClientRequestFactory requestFactory) {
        this.jsonService = jsonService;
        this.responseMapper = responseMapper;
        this.asyncGithubUserHandler = asyncGithubUserHandler;
        this.asyncGithubUserRepoHandler = asyncGithubUserRepoHandler;
        this.requestFactory = requestFactory;
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public GithubUserApiService githubUserApiService() {
        if (this.handleAsync) {
            return new AsyncCachingGithubUserService(
                    this.httpClient(),
                    this.jsonService,
                    this.responseMapper,
                    this.requestFactory,
                    this.asyncGithubUserHandler,
                    this.asyncGithubUserRepoHandler);
        }

        return new CachingGithubUserApiService(
                this.httpClient(), this.jsonService, this.responseMapper, this.requestFactory);
    }
}
