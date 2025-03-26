package com.row49382.service.impl;

import com.row49382.domain.dto.github.response.GithubUserRepositoryResponse;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.test_util.ExpectedTestJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CachingGithubUserApiServiceTest extends AbstractGithubUserApiServiceTest {

    @BeforeEach
    public void setup() {
        this.githubUserApiService =
                new CachingGithubUserApiService(this.client, this.jsonService, this.responseMapper, this.requestFactory);
    }

    @Test
    void verifyGithubUsernameFetchSuccess() throws Throwable {
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

    protected HttpResponse<String> mockGithubUserValidResponse() throws IOException, InterruptedException {
        return this.mockClientResponse(GITHUB_USER_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, VALID_USERNAME, 200);
    }

    protected HttpResponse<String> mockGithubUserRepoValidResponse() throws IOException, InterruptedException {
        return this.mockClientResponse(GITHUB_USER_REPO_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_REPO_LIST_JSON, VALID_USERNAME, 200);
    }

    protected HttpResponse<String> mockGithubUserNotFoundResponse() throws IOException, InterruptedException {
        return this.mockClientResponse(GITHUB_USER_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, INVALID_USERNAME, 500);
    }

    protected HttpResponse<String> mockGithubUserResponseNotValidJsonResponse() throws IOException, InterruptedException {
        return this.mockClientResponse(GITHUB_USER_ENDPOINT_TEMPLATE, "invalid_json", VALID_USERNAME, 200);
    }

    protected HttpResponse<String> mockClientResponse(String endpoint, String expectedResponse, String username, int statusCode)
            throws IOException, InterruptedException {
        HttpResponse<String> response = mock(HttpResponse.class);
        lenient().when(response.statusCode()).thenReturn(statusCode);
        lenient().when(response.body()).thenReturn(expectedResponse);

        HttpRequest githubUserRequest =
                this.requestFactory.buildRequest(endpoint.formatted(username));

        when(this.client.send(githubUserRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(response);

        return response;
    }
}
