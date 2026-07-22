package com.fixit.platform.modules.request.exception;

public class UnauthorizedRequestActionException extends RuntimeException {

    public UnauthorizedRequestActionException(String message) {
        super(message);
    }
}