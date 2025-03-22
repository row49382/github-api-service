package com.row49382.exception;

import java.io.Serial;

public class GithubUserFetchException extends Exception {
    @Serial
    private static final long serialVersionUID = -278156538044821175L;

    public GithubUserFetchException(String message) {
        super(message);
    }

    public GithubUserFetchException(String message, Throwable th) {
        super(message, th);
    }
}
