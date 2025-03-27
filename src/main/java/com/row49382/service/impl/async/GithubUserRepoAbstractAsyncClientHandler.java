package com.row49382.service.impl.async;

import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.service.AbstractAsyncRESTHandler;
import com.row49382.service.JsonService;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.List;

@Component
public class GithubUserRepoAbstractAsyncClientHandler extends AbstractAsyncRESTHandler<List<GithubUserRepoResponse>> {
    public GithubUserRepoAbstractAsyncClientHandler(
            HttpClient client,
            GitHubUserHttpClientRequestFactory requestFactory,
            JsonService jsonService) {
        super(client, requestFactory, jsonService);
    }

    @Override
    protected List<GithubUserRepoResponse> deserializeResponse(String body) {
        return this.jsonService.deserializeList(body, GithubUserRepoResponse.class);
    }
}
