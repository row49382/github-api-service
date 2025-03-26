package com.row49382.service;

import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;

public interface GithubUserApiService {
    GithubUserAggregatedResponse fetchByUsername(String username) throws Throwable;
}
