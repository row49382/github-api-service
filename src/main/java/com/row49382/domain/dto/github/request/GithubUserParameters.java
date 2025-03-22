package com.row49382.domain.dto.github.request;

import com.row49382.validation.NoHtml;
import jakarta.validation.constraints.NotEmpty;

public class GithubUserParameters {
    @NotEmpty(message = "username can not be empty")
    @NoHtml
    private String username;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
