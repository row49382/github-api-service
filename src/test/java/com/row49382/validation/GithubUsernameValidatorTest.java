package com.row49382.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GithubUsernameValidatorTest {
    private GithubUsernameValidator githubUsernameValidator;

    @BeforeEach
    public void setup() {
        this.githubUsernameValidator = new GithubUsernameValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "<script></script>",
            "-octocat",
            "octocat-",
            "-octocat-",
            "octo--cat",
            "!@#$%^&*()_-+=|\\<>/,.",
            "longerthan39charactersssssssssssssssssss"
    })
    void verifyInvalidUsernames(String invalidUsername) {
        assertFalse(this.githubUsernameValidator.isValid(invalidUsername, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "octocat",
            "octo-cat",
            "123456789",
            "asdfg12345"
    })
    void verifyValidUsernames(String validUsername) {
        assertTrue(this.githubUsernameValidator.isValid(validUsername, null));
    }
}
