package com.row49382.domain.dto.github.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonPropertyOrder({
        "user_name",
        "display_name",
        "avatar",
        "geo_location",
        "email",
        "url",
        "created_at",
        "repos"
})
public class GithubUserAggregatedResponse {
    @JsonProperty("user_name")
    private String username;
    @JsonProperty("display_name")
    private String displayName;
    private String avatar;
    @JsonProperty("geo_location")
    private String geoLocation;
    private String email;
    private String url;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    @JsonProperty("created_at")
    private Date createdAt;
    private List<GithubUserRepositoryResponse> repos;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<GithubUserRepositoryResponse> getRepos() {
        return repos;
    }

    public void addRepository(GithubUserRepositoryResponse githubRepository) {
        if (this.repos == null) {
            this.repos = new ArrayList<>();
        }

        this.repos.add(githubRepository);
    }
}
