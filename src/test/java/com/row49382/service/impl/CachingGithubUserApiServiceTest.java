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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CachingGithubUserApiServiceTest {
    private static final String VALID_USERNAME = "valid";
    private static final String INVALID_USERNAME = "invalid";
    private static final String GITHUB_USER_ENDPOINT_TEMPLATE = "/users/%s";
    private static final String GITHUB_USER_REPO_ENDPOINT_TEMPLATE = GITHUB_USER_ENDPOINT_TEMPLATE + "/repos";

    @Autowired
    private JsonService jsonService;

    @Autowired
    private BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> responseMapper;

    @Mock
    private HttpClient client;

    private CachingGithubUserApiService githubUserApiService;

    @BeforeEach
    public void setup() {
        this.githubUserApiService =
                new CachingGithubUserApiService(this.client, this.jsonService, this.responseMapper);

        ReflectionTestUtils.setField(this.githubUserApiService, "githubUrl", "https://api.github.com");
        ReflectionTestUtils.setField(this.githubUserApiService, "githubUserAgent", "test-user-agent");
    }

    @Test
    void verifyGithubUsernameFetchSuccess() throws GithubUserFetchException, JsonDeserializationException, IOException, InterruptedException {
        HttpResponse<String> githubUserResponse = this.mockGithubUserValidResponse();
        HttpResponse<String> githubUserRepoResponse = this.mockGithubUserRepoValidResponse();

        GithubUserAggregatedResponse githubUserAggregatedResponse =
                this.githubUserApiService.fetchByUsername(VALID_USERNAME);

        verify(githubUserResponse).statusCode();
        verify(githubUserResponse).body();
        verify(githubUserRepoResponse).statusCode();
        verify(githubUserResponse).body();
        verify(this.client, times(2)).send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));

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
    void verifyWhenGithubUsernameNotFoundThenFetchExceptionIsThrown() throws IOException, InterruptedException {
        HttpResponse<String> githubUserResponse = this.mockGithubUserNotFoundResponse();

        assertThrows(
                GithubUserFetchException.class,
                () -> this.githubUserApiService.fetchByUsername(INVALID_USERNAME));

        verify(githubUserResponse).statusCode();
        verify(githubUserResponse, never()).body();
        verify(this.client).send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
    }

    @Test
    void verifyWhenGithubUsernameResponseInvalidJsonThenDeserializationExceptionIsThrown() throws IOException, InterruptedException {
        HttpResponse<String> githubUserResponse = this.mockGithubUserResponseNotValidJsonResponse();

        assertThrows(
                JsonDeserializationException.class,
                () -> this.githubUserApiService.fetchByUsername(VALID_USERNAME));

        verify(githubUserResponse).statusCode();
        verify(githubUserResponse).body();
        verify(this.client).send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            InterruptedException.class,
            IOException.class
    })
    void verifyWhenHttpClientThrowsExceptionThenFetchExceptionIsThrown(Class<Throwable> httpClientException) throws IOException, InterruptedException {
        when(this.client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(httpClientException);

        assertThrows(
                GithubUserFetchException.class,
                () -> this.githubUserApiService.fetchByUsername(""));

        verify(this.client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    private HttpResponse<String> mockGithubUserValidResponse() throws IOException, InterruptedException {
        return this.mockClientResponse(GITHUB_USER_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, VALID_USERNAME, 200);
    }

    private HttpResponse<String> mockGithubUserRepoValidResponse() throws IOException, InterruptedException {
        return this.mockClientResponse(GITHUB_USER_REPO_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_REPO_LIST_JSON, VALID_USERNAME, 200);
    }

    private HttpResponse<String> mockGithubUserNotFoundResponse() throws IOException, InterruptedException {
        return this.mockClientResponse(GITHUB_USER_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, INVALID_USERNAME, 404);
    }

    private HttpResponse<String> mockGithubUserResponseNotValidJsonResponse() throws IOException, InterruptedException {
        return this.mockClientResponse(GITHUB_USER_ENDPOINT_TEMPLATE, "invalid_json", VALID_USERNAME, 200);
    }

    private HttpResponse<String> mockClientResponse(String endpoint, String expectedResponse, String username, int statusCode) throws IOException, InterruptedException {
        HttpResponse<String> response = mock(HttpResponse.class);
        lenient().when(response.statusCode()).thenReturn(statusCode);
        lenient().when(response.body()).thenReturn(expectedResponse);

        HttpRequest githubUserRequest =
                this.githubUserApiService.buildRequest(endpoint.formatted(username));

        when(this.client.send(githubUserRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(response);

        return response;
    }
}
