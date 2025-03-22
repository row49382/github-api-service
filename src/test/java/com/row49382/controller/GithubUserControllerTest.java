package com.row49382.controller;

import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.dto.github.response.GithubUserRepositoryResponse;
import com.row49382.exception.JsonDeserializationException;
import com.row49382.service.JsonService;
import com.row49382.test_util.ExpectedTestJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "github-api-url=https://api.github.com",
        "github-user-agent=test-user-agent"
})
@AutoConfigureMockMvc
class GithubUserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonService jsonService;

    @Test
    void verifyGetByUsernameSuccessful() throws Exception {
        GithubUserAggregatedResponse expected =
                this.jsonService.deserialize(ExpectedTestJson.APPLICATION_GITHUB_USER_RESPONSE, GithubUserAggregatedResponse.class);

        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.get("/api/github/users?username=%s".formatted("octocat"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        GithubUserAggregatedResponse actual =
                this.jsonService.deserialize(result.getResponse().getContentAsString(), GithubUserAggregatedResponse.class);

        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getAvatar(), actual.getAvatar());
        assertEquals(expected.getGeoLocation(), actual.getGeoLocation());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());

        for (int i = 0; i < expected.getRepos().size(); i++) {
            GithubUserRepositoryResponse expectedRepo = expected.getRepos().get(i);
            GithubUserRepositoryResponse actualRepo = actual.getRepos().get(i);

            assertEquals(expectedRepo.getName(), actualRepo.getUrl());
            assertEquals(expectedRepo.getUrl(), actualRepo.getUrl());
        }

    }

    @Test
    void verifyInvalidUsernameProvidedReturnsBadRequest() throws Exception {
        String invalidUsername = "<script></script>";

        this.mvc.perform(MockMvcRequestBuilders.get("/api/github/users?username=%s".formatted(invalidUsername))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
