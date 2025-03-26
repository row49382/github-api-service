package com.row49382.service.impl;

import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.mapper.BiMapper;
import com.row49382.service.GithubUserApiService;
import com.row49382.service.JsonService;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.http.HttpClient;
import java.util.List;

@SpringBootTest
abstract class AbstractGithubUserApiServiceTest {
    protected static final String VALID_USERNAME = "valid";
    protected static final String INVALID_USERNAME = "invalid";
    protected static final String GITHUB_USER_ENDPOINT_TEMPLATE = "/users/%s";
    protected static final String GITHUB_USER_REPO_ENDPOINT_TEMPLATE = GITHUB_USER_ENDPOINT_TEMPLATE + "/repos";

    @Autowired
    protected JsonService jsonService;

    @Autowired
    protected BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper;

    @Autowired
    protected GitHubUserHttpClientRequestFactory requestFactory;

    @Mock
    protected HttpClient client;

    protected GithubUserApiService githubUserApiService;
}
