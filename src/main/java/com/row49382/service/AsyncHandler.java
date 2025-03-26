package com.row49382.service;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface AsyncHandler<T> {
    CompletableFuture<HttpResponse<T>> handle(String endpoint);
}
