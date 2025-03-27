package com.row49382.service.impl.async;

import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.service.AsyncRESTHandler;
import com.row49382.service.JsonService;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
public class GithubUserAsyncClientHandler extends AsyncRESTHandler<GithubUserResponse> {
    public GithubUserAsyncClientHandler(
            HttpClient client,
            GitHubUserHttpClientRequestFactory requestFactory,
            JsonService jsonService) {
        super(client, requestFactory, input -> jsonService.deserialize(input, GithubUserResponse.class));
    }
}
