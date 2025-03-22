package com.row49382.mapper.impl;

import com.row49382.domain.dto.github.response.GithubUserRepositoryResponse;
import com.row49382.domain.dto.github.response.GithubUserAggregatedResponse;
import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.mapper.BiMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GithubUserAndRepoResponseToDTOMapper
        implements BiMapper<GithubUserResponse, List<GithubUserRepoResponse>, GithubUserAggregatedResponse> {

    @Override
    public GithubUserAggregatedResponse map(
            GithubUserResponse githubUserResponse,
            List<GithubUserRepoResponse> githubUserRepoResponses) {
        if (githubUserResponse == null) {
            return null;
        }

        GithubUserAggregatedResponse response = new GithubUserAggregatedResponse();

        response.setUsername(githubUserResponse.getLogin());
        response.setDisplayName(githubUserResponse.getName());
        response.setAvatar(githubUserResponse.getAvatarUrl());
        response.setGeoLocation(githubUserResponse.getLocation());
        response.setEmail(githubUserResponse.getEmail());
        response.setUrl(githubUserResponse.getHtmlUrl());
        response.setCreatedAt(githubUserResponse.getCreatedAt());

        for (GithubUserRepoResponse repoResponse : githubUserRepoResponses) {
            GithubUserRepositoryResponse githubRepository = new GithubUserRepositoryResponse();

            githubRepository.setName(repoResponse.getName());
            githubRepository.setUrl(repoResponse.getHtmlUrl());

            response.addRepository(githubRepository);
        }

        return response;
    }
}
