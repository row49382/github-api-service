package com.row49382.exception;

import java.io.Serial;

public class JsonDeserializationException extends Exception {
    @Serial
    private static final long serialVersionUID = -8600953961212069671L;

    public JsonDeserializationException(String message, Throwable th) {
        super(message, th);
    }
}
