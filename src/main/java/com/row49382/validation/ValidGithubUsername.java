package com.row49382.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = { GithubUsernameValidator.class })
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGithubUsername {
    String message() default "Not a valid GitHub username";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

