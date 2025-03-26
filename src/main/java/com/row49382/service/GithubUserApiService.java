package com.row49382.service;

import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.exception.JsonDeserializationException;

public interface GithubUserApiService {
    GithubUserAggregatedResponse fetchByUsername(String username) throws Throwable;
}
