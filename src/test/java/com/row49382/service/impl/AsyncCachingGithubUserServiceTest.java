package com.row49382.service.impl;

import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.dto.github.response.GithubUserRepositoryResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.service.AbstractAsyncRESTHandler;
import com.row49382.service.impl.async.AsyncCachingGithubUserService;
import com.row49382.service.impl.async.GithubUserAbstractAsyncClientHandler;
import com.row49382.service.impl.async.GithubUserRepoAbstractAsyncClientHandler;
import com.row49382.test_util.ExpectedTestJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AsyncCachingGithubUserServiceTest extends AbstractGithubUserApiServiceTest {
    @BeforeEach
    public void setup() {
        AbstractAsyncRESTHandler<GithubUserResponse> asyncGithubUserHandler =
                new GithubUserAbstractAsyncClientHandler(this.client, this.requestFactory, this.jsonService);
        AbstractAsyncRESTHandler<List<GithubUserRepoResponse>> asyncGithubUserRepoHandler =
                new GithubUserRepoAbstractAsyncClientHandler(this.client, this.requestFactory, this.jsonService);

        this.githubUserApiService =
                new AsyncCachingGithubUserService(this.client, this.jsonService, this.responseMapper, this.requestFactory, asyncGithubUserHandler, asyncGithubUserRepoHandler);
    }
    @Test
    void verifyGithubUsernameFetchSuccessAsync() throws Throwable {
        CompletableFuture<HttpResponse<String>> githubUserResponse = this.mockGithubUserValidResponseAsync();
        CompletableFuture<HttpResponse<String>> githubUserRepoResponse = this.mockGithubUserRepoValidResponseAsync();

        GithubUserAggregatedResponse githubUserAggregatedResponse =
                this.githubUserApiService.fetchByUsername(VALID_USERNAME);

        HttpResponse<String> expectedUserResponse = githubUserResponse.get();
        HttpResponse<String> expectedUserRepoResponse = githubUserRepoResponse.get();

        verify(expectedUserResponse).statusCode();
        verify(expectedUserResponse).body();
        verify(expectedUserRepoResponse).statusCode();
        verify(expectedUserRepoResponse).body();
        verify(this.client, times(2)).sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));

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
    void verifyWhenGithubUsernameNotFoundThenFetchExceptionIsThrownAsync() throws InterruptedException, ExecutionException {
        CompletableFuture<HttpResponse<String>> githubUserResponse =
                this.mockGithubUserNotFoundResponseAsync();
        CompletableFuture<HttpResponse<String>> githubUserRepoResponse =
                this.mockGithubUserRepoNotFoundResponseAsync();

        assertThrows(
                GithubUserFetchException.class,
                () -> this.githubUserApiService.fetchByUsername(INVALID_USERNAME));

        HttpResponse<String> expectedUserResponse = githubUserResponse.get();
        HttpResponse<String> expectedUserRepoResponse = githubUserRepoResponse.get();

        verify(expectedUserResponse).statusCode();
        verify(expectedUserResponse).uri();
        verify(expectedUserRepoResponse).statusCode();
        verify(expectedUserRepoResponse).uri();
        verify(this.client, times(2)).sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
    }

    @Test
    void verifyWhenGithubUsernameResponseInvalidJsonThenDeserializationExceptionIsThrownAsync() throws InterruptedException, ExecutionException {
        CompletableFuture<HttpResponse<String>> githubUserResponse =
                this.mockGithubUserResponseNotValidJsonResponseAsync();
        CompletableFuture<HttpResponse<String>> githubUserRepoResponse =
                this.mockGithubUserRepoValidResponseAsync();

        assertThrows(
                JsonDeserializationException.class,
                () -> this.githubUserApiService.fetchByUsername(VALID_USERNAME));

        HttpResponse<String> expectedUserResponse = githubUserResponse.get();
        HttpResponse<String> expectedUserRepoResponse = githubUserRepoResponse.get();

        verify(expectedUserResponse).statusCode();
        verify(expectedUserResponse).body();
        verify(expectedUserRepoResponse).statusCode();
        verify(expectedUserRepoResponse).body();
        verify(this.client, times(2)).sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
    }

    @ParameterizedTest
    @MethodSource("getFutureExceptions")
    void verifyWhenHttpClientThrowsExceptionThenFetchExceptionIsThrownAsync(Throwable futureException) throws InterruptedException, ExecutionException {
        CompletableFuture<HttpResponse<String>> future = mock(CompletableFuture.class);
        lenient().when(future.thenCombine(any(CompletionStage.class), any(BiFunction.class))).thenReturn(future);

        when(this.client.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(future);

        lenient().when(future.thenApply(any(Function.class))).thenReturn(future);
        when(future.get()).thenThrow(futureException);

        assertThrows(
                GithubUserFetchException.class,
                () -> this.githubUserApiService.fetchByUsername("octocat"));

        verify(future).get();
        verify(this.client, times(2)).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    private static Stream<Arguments> getFutureExceptions() {
        return Stream.of(
                Arguments.of(new InterruptedException()),
                Arguments.of(new ExecutionException(new GithubUserFetchException("")))
        );
    }

    protected CompletableFuture<HttpResponse<String>> mockGithubUserValidResponseAsync() {
        return this.mockClientResponseAsync(GITHUB_USER_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, VALID_USERNAME, 200);
    }

    protected CompletableFuture<HttpResponse<String>> mockGithubUserRepoValidResponseAsync() {
        return this.mockClientResponseAsync(GITHUB_USER_REPO_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_REPO_LIST_JSON, VALID_USERNAME, 200);
    }

    protected CompletableFuture<HttpResponse<String>> mockGithubUserRepoNotFoundResponseAsync() {
        return this.mockClientResponseAsync(GITHUB_USER_REPO_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_REPO_LIST_JSON, INVALID_USERNAME, 500);
    }

    protected CompletableFuture<HttpResponse<String>> mockGithubUserNotFoundResponseAsync() {
        return this.mockClientResponseAsync(GITHUB_USER_ENDPOINT_TEMPLATE, ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, INVALID_USERNAME, 500);
    }

    protected CompletableFuture<HttpResponse<String>> mockGithubUserResponseNotValidJsonResponseAsync() {
        return this.mockClientResponseAsync(GITHUB_USER_ENDPOINT_TEMPLATE, "invalid_json", VALID_USERNAME, 200);
    }

    protected CompletableFuture<HttpResponse<String>> mockClientResponseAsync(String endpoint, String expectedResponse, String username, int statusCode) {
        HttpResponse<String> response = mock(HttpResponse.class);
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(response);

        lenient().when(response.statusCode()).thenReturn(statusCode);
        lenient().when(response.body()).thenReturn(expectedResponse);
        lenient().when(response.uri()).thenReturn(URI.create("https://api.github.com"));

        HttpRequest githubUserRequest =
                this.requestFactory.buildRequest(endpoint.formatted(username));

        when(this.client.sendAsync(githubUserRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(future);

        return future;
    }
}
