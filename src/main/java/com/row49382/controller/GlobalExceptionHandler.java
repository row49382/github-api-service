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
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Unexpected error occurred")
    public ResponseEntity<ErrorResponse> handleRunTimeException(RuntimeException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid input")
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getBody().getDetail()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonDeserializationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unable to deserialize response from json api. Check the username in the request")
    public ResponseEntity<ErrorResponse> handleJsonDeserializationFailure(JsonDeserializationException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GithubUserFetchException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Unable to fetch github user")
    public ResponseEntity<ErrorResponse> handleGithubUserFetchFailure(GithubUserFetchException ex) {
        return ResponseEntity.ofNullable(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }
}
