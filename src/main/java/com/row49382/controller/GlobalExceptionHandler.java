package com.row49382.controller;

import com.row49382.domain.dto.ErrorResponse;
import com.row49382.exception.GithubUserFetchException;
import com.row49382.exception.JsonDeserializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleRunTimeException(RuntimeException ex) {
        return ResponseEntity.ofNullable(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ResponseEntity.ofNullable(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getBody().getDetail()));
    }

    @ExceptionHandler(JsonDeserializationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleJsonDeserializationFailure(JsonDeserializationException ex) {
        return ResponseEntity.ofNullable(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(GithubUserFetchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleGithubUserFetchFailure(GithubUserFetchException ex) {
        return ResponseEntity.ofNullable(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }
}
