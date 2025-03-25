package com.row49382.mapper;

import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.mapper.impl.GithubUserAndRepoResponseToDTOMapper;
import com.row49382.service.JsonService;
import com.row49382.test_util.ExpectedTestJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GithubUserAndRepoResponseToDTOMapperTest {

    @Autowired
    private JsonService jsonService;

    private GithubUserAndRepoResponseToDTOMapper mapper;

    @BeforeEach
    public void setup() {
        this.mapper = new GithubUserAndRepoResponseToDTOMapper();
    }

    @Test
    void verifyMapperSuccess() throws JsonDeserializationException {
        GithubUserResponse expectedGithubUserResponse =
                this.jsonService.deserialize(ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, GithubUserResponse.class);
        List<GithubUserRepoResponse> expectedGithubUserRepoResponse =
                this.jsonService.deserializeList(ExpectedTestJson.GITHUB_USER_REPO_LIST_JSON, GithubUserRepoResponse.class);
        GithubUserRepoResponse expectedRepo1 = expectedGithubUserRepoResponse.get(0);
        GithubUserRepoResponse expectedRepo2 = expectedGithubUserRepoResponse.get(1);

        GithubUserAggregatedResponse actual = this.mapper.map(expectedGithubUserResponse, expectedGithubUserRepoResponse);

        assertEquals(expectedGithubUserResponse.getLogin(), actual.getUsername());
        assertEquals(expectedGithubUserResponse.getName(), actual.getDisplayName());
        assertEquals(expectedGithubUserResponse.getAvatarUrl(), actual.getAvatar());
        assertEquals(expectedGithubUserResponse.getLocation(), actual.getGeoLocation());
        assertEquals(expectedGithubUserResponse.getEmail(), actual.getEmail());
        assertEquals(expectedGithubUserResponse.getHtmlUrl(), actual.getUrl());
        assertEquals(expectedGithubUserResponse.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expectedRepo1.getName(), actual.getRepos().get(0).getName());
        assertEquals(expectedRepo1.getHtmlUrl(), actual.getRepos().get(0).getUrl());
        assertEquals(expectedRepo2.getName(), actual.getRepos().get(1).getName());
        assertEquals(expectedRepo2.getHtmlUrl(), actual.getRepos().get(1).getUrl());
    }

    @Test
    void verifyResultNullWhenGithubUserResponseIsNull() throws JsonDeserializationException {
        GithubUserResponse expectedGithubUserResponse = null;
        List<GithubUserRepoResponse> expectedGithubUserRepoResponse =
                this.jsonService.deserializeList(ExpectedTestJson.GITHUB_USER_REPO_LIST_JSON, GithubUserRepoResponse.class);
        GithubUserAggregatedResponse actual = this.mapper.map(expectedGithubUserResponse, expectedGithubUserRepoResponse);

        assertNull(actual);
    }

    @Test
    void verifyRepoResultsNullWhenGithubUserRepoListResponseIsNull() throws JsonDeserializationException {
        GithubUserResponse expectedGithubUserResponse =
                this.jsonService.deserialize(ExpectedTestJson.GITHUB_USER_RESPONSE_JSON, GithubUserResponse.class);
        List<GithubUserRepoResponse> expectedGithubUserRepoResponse = null;

        GithubUserAggregatedResponse actual = this.mapper.map(expectedGithubUserResponse, expectedGithubUserRepoResponse);

        assertEquals(expectedGithubUserResponse.getLogin(), actual.getUsername());
        assertEquals(expectedGithubUserResponse.getName(), actual.getDisplayName());
        assertEquals(expectedGithubUserResponse.getAvatarUrl(), actual.getAvatar());
        assertEquals(expectedGithubUserResponse.getLocation(), actual.getGeoLocation());
        assertEquals(expectedGithubUserResponse.getEmail(), actual.getEmail());
        assertEquals(expectedGithubUserResponse.getHtmlUrl(), actual.getUrl());
        assertEquals(expectedGithubUserResponse.getCreatedAt(), actual.getCreatedAt());
        assertNull(actual.getRepos());
    }
}
