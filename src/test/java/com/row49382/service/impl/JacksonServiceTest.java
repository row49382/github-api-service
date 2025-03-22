package com.row49382.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.service.JsonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.row49382.test_util.ExpectedTestJson.GITHUB_USER_RESPONSE_JSON;
import static com.row49382.test_util.ExpectedTestJson.GITHUB_USER_REPO_LIST_JSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JacksonServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    private JsonService jsonService;

    @BeforeEach
    public void setup() {
        this.jsonService = new JacksonService(new ObjectMapper());
    }

    @Test
    void verifyJsonDeserializesSuccess() throws JsonDeserializationException {
        GithubUserResponse actual = this.jsonService.deserialize(GITHUB_USER_RESPONSE_JSON, GithubUserResponse.class);

        assertNotNull(actual);
        assertEquals("octocat", actual.getLogin());
        assertEquals("https://avatars.githubusercontent.com/u/583231?v=4", actual.getAvatarUrl());
        assertEquals("https://github.com/octocat", actual.getHtmlUrl());
        assertEquals("The Octocat", actual.getName());
        assertEquals("San Francisco", actual.getLocation());
        assertEquals("2011-01-25T18:44:36Z", actual.getCreatedAt().toInstant().toString());
        assertNull(actual.getEmail());
    }

    @Test
    void verifyJsonListDeserializesSuccess() throws JsonDeserializationException {
        List<GithubUserRepoResponse> actual =
                this.jsonService.deserializeList(GITHUB_USER_REPO_LIST_JSON, GithubUserRepoResponse.class);

        GithubUserRepoResponse firstRepo = actual.get(0);
        GithubUserRepoResponse secondRepo = actual.get(1);

        assertNotNull(firstRepo);
        assertEquals("boysenberry-repo-1", firstRepo.getName());
        assertEquals("https://github.com/octocat/boysenberry-repo-1", firstRepo.getHtmlUrl());

        assertNotNull(secondRepo);
        assertEquals("git-consortium", secondRepo.getName());
        assertEquals("https://github.com/octocat/git-consortium", secondRepo.getHtmlUrl());
    }

    @Test
    void verifyWhenJsonListIsNotListThenThrowException() {
        assertThrows(
                JsonDeserializationException.class,
                () -> this.jsonService.deserializeList(GITHUB_USER_RESPONSE_JSON, GithubUserRepoResponse.class));
    }

    @Test
    void verifyWhenJacksonExceptionThrownThenDomainExceptionIsThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(anyString(), any(Class.class)))
                .thenThrow(JsonParseException.class);

        JsonService jsonService = new JacksonService(objectMapper);

        assertThrows(
                JsonDeserializationException.class,
                () -> jsonService.deserialize("", Object.class));
    }

}
