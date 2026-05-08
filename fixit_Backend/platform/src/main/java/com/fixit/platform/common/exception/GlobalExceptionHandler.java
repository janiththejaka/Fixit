package com.fixit.platform.common.exception;

import com.fixit.platform.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidCredentials(
            InvalidCredentialsException ex
    ) {

        ApiResponse<?> response =
                new ApiResponse<>(false, ex.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(
            ResourceNotFoundException ex
    ) {

        ApiResponse<?> response =
                new ApiResponse<>(false, ex.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleEmailExists(
            EmailAlreadyExistsException ex
    ) {

        ApiResponse<?> response =
                new ApiResponse<>(false, ex.getMessage(), null);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(
            Exception ex
    ) {

        ApiResponse<?> response =
                new ApiResponse<>(false, ex.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
