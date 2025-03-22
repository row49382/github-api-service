package com.row49382.service.impl;

import com.row49382.domain.dto.github.response.GithubUserRepositoryResponse;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.mapper.BiMapper;
import com.row49382.service.JsonService;
import com.row49382.test_util.ExpectedTestJson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "github-api-url=https://api.github.com",
        "github-user-agent=test-user-agent"
})
class CachingGithubUserApiServiceTest {
    private static final String VALID_USERNAME = "valid";
    private static final String INVALID_USERNAME = "invalid";
    private static final String GITHUB_USER_ENDPOINT_TEMPLATE = "/users/%s";
    private static final String GITHUB_USER_REPO_ENDPOINT_TEMPLATE = GITHUB_USER_ENDPOINT_TEMPLATE + "/repos";

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private JsonService jsonService;

    @Autowired
    private BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper;

    @MockitoBean
    private HttpClient client;

    @Autowired
    private CachingGithubUserApiService githubUserApiService;

    @Test
    void verifyGithubUsernameFetchSuccess() throws GithubUserFetchException, JsonDeserializationException, IOException, InterruptedException {
        this.mockGithubUserValidRequest();
        this.mockGithubUserRepoValidRequest();

        GithubUserAggregatedResponse githubUserAggregatedResponse =
                this.githubUserApiService.fetchByUsername(VALID_USERNAME);

        assertNotNull(githubUserAggregatedResponse);
        assertEquals("octocat", githubUserAggregatedResponse.getUsername());
        assertEquals("https://avatars.githubusercontent.com/u/583231?v=4", githubUserAggregatedResponse.getAvatar());
        assertEquals("https://github.com/octocat", githubUserAggregatedResponse.getUrl());
        assertEquals("The Octocat", githubUserAggregatedResponse.getDisplayName());
        assertEquals("San Francisco", githubUserAggregatedResponse.getGeoLocation());
        assertEquals("2011-01-25T18:44:36Z", githubUserAggregatedResponse.getCreatedAt().toInstant().toString());
        assertNull(githubUserAggregatedResponse.getEmail());

        GithubUserRepositoryResponse firstRepo = githubUserAggregatedResponse.getRepos().get(0);
        GithubUserRepositoryResponse secondRepo = githubUserAggregatedResponse.getRepos().get(1);

        assertNotNull(firstRepo);
        assertEquals("boysenberry-repo-1", firstRepo.getName());
        assertEquals("https://github.com/octocat/boysenberry-repo-1", firstRepo.getUrl());

        assertNotNull(secondRepo);
        assertEquals("git-consortium", secondRepo.getName());
        assertEquals("https://github.com/octocat/git-consortium", secondRepo.getUrl());
    }

    @Test
    void verifyWhenGithubUsernameProvidedMoreThanOnceSameObjectIsReturned() throws IOException, InterruptedException, GithubUserFetchException, JsonDeserializationException {
        this.mockGithubUserValidRequest();
        this.mockGithubUserRepoValidRequest();

        GithubUserAggregatedResponse originalResponse =
                this.githubUserApiService.fetchByUsername(VALID_USERNAME);

        GithubUserAggregatedResponse cachedResponse =
                this.githubUserApiService.fetchByUsername(VALID_USERNAME);

        assertEquals(originalResponse, cachedResponse);
    }

    @Test
    void verifyWhenGithubUsernameNotFoundThenFetchExceptionIsThrown() throws IOException, InterruptedException {
        this.mockGithubUserNotFoundRequest();
        this.mockGithubUserRepoValidRequest();

        assertThrows(
                GithubUserFetchException.class,
                () -> this.githubUserApiService.fetchByUsername(INVALID_USERNAME));
    }

    @Test
    void verifyWhenGithubUsernameResponseInvalidJsonThenDeserializationExceptionIsThrown() throws IOException, InterruptedException {
        this.mockGithubUserResponseNotValidJsonRequest();
        this.mockGithubUserRepoValidRequest();

        assertThrows(
                JsonDeserializationException.class,
                () -> this.githubUserApiService.fetchByUsername(VALID_USERNAME));
    }

    private void mockGithubUserValidRequest() throws IOException, InterruptedException {
        this.mockRequest(GITHUB_USER_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, VALID_USERNAME, 200);
    }

    private void mockGithubUserRepoValidRequest() throws IOException, InterruptedException {
        this.mockRequest(GITHUB_USER_REPO_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_REPO_LIST_JSON, VALID_USERNAME, 200);
    }

    private void mockGithubUserNotFoundRequest() throws IOException, InterruptedException {
        this.mockRequest(GITHUB_USER_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, INVALID_USERNAME, 404);
    }

    private void mockGithubUserResponseNotValidJsonRequest() throws IOException, InterruptedException {
        this.mockRequest(GITHUB_USER_ENDPOINT_TEMPLATE, "invalid_json", VALID_USERNAME, 200);
    }

    private void mockRequest(String endpoint, String expectedResponse, String username, int statusCode) throws IOException, InterruptedException {
        HttpResponse<String> githubUserResponse = mock(HttpResponse.class);
        when(githubUserResponse.statusCode()).thenReturn(statusCode);
        when(githubUserResponse.body()).thenReturn(expectedResponse);

        HttpRequest githubUserRequest =
                this.githubUserApiService.buildRequest(endpoint.formatted(username));

        when(this.client.send(githubUserRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(githubUserResponse);
    }
}
