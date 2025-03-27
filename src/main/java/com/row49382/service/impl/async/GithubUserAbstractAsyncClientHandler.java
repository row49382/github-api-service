package com.row49382.service.impl.async;

import com.row49382.domain.third_party.github.dto.GithubUserResponse;
import com.row49382.service.AbstractAsyncRESTHandler;
import com.row49382.service.JsonService;
import com.row49382.service.impl.GitHubUserHttpClientRequestFactory;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
public class GithubUserAbstractAsyncClientHandler extends AbstractAsyncRESTHandler<GithubUserResponse> {
    public GithubUserAbstractAsyncClientHandler(
            HttpClient client,
            GitHubUserHttpClientRequestFactory requestFactory,
            JsonService jsonService) {
        super(client, requestFactory, jsonService);
    }

    @Override
    protected GithubUserResponse deserializeResponse(String body) {
        return this.jsonService.deserialize(body, GithubUserResponse.class);
    }
}
