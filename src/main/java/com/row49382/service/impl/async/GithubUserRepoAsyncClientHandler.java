package com.row49382.service.impl.async;

import com.row49382.domain.third_party.github.dto.GithubUserRepoResponse;
import com.row49382.service.AsyncRESTHandler;
import com.row49382.service.JsonService;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.List;

@Component
public class GithubUserRepoAsyncClientHandler extends AsyncRESTHandler<List<GithubUserRepoResponse>> {
    public GithubUserRepoAsyncClientHandler(
            HttpClient client,
            GitHubUserHttpClientRequestFactory requestFactory,
            JsonService jsonService) {
        super(client, requestFactory, input -> jsonService.deserializeList(input, GithubUserRepoResponse.class));
    }
}
