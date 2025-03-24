package com.row49382.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GithubUsernameValidator implements ConstraintValidator<ValidGithubUsername, String> {
    /**
     * According to GitHub: Username may only contain alphanumeric characters or single hyphens,
     * and cannot begin or end with a hyphen. The max length of their username field is also 39.
     * <p>
     * This regex captures that criteria.
     */
    private static final String GITHUB_USERNAME_REGEX = "^[a-zA-Z0-9]([a-zA-Z0-9]|-[a-zA-Z0-9]){0,38}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.matches(GITHUB_USERNAME_REGEX);
    }
}

