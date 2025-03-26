package com.row49382.controller;

import com.row49382.domain.dto.github.request.GithubUserParameters;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.service.GithubUserApiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github/users")
public class GithubUserController {
    private final GithubUserApiService githubUserApiService;

    public GithubUserController(GithubUserApiService githubUserApiService) {
        this.githubUserApiService = githubUserApiService;
    }

    @GetMapping
    public ResponseEntity<GithubUserAggregatedResponse> getGithubUser(@Valid GithubUserParameters params)
            throws Throwable {
        return ResponseEntity.ofNullable(
                this.githubUserApiService.fetchByUsername(params.getUsername()));
    }
}
