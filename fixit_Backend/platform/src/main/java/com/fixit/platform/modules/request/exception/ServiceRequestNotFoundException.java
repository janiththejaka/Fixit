package com.fixit.platform.modules.request.exception;

public class ServiceRequestNotFoundException extends RuntimeException {

    public ServiceRequestNotFoundException(String message) {
        super(message);
    }
}