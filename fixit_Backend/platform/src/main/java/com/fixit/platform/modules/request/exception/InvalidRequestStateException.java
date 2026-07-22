package com.fixit.platform.modules.request.exception;

public class InvalidRequestStateException extends RuntimeException {

    public InvalidRequestStateException(String message) {
        super(message);
    }
}