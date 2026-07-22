package com.fixit.platform.common.exception;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.request.exception.InvalidRequestStateException;
import com.fixit.platform.modules.request.exception.ServiceRequestNotFoundException;
import com.fixit.platform.modules.request.exception.UnauthorizedRequestActionException;
import org.springframework.security.access.AccessDeniedException;
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(
            AccessDeniedException ex
    ) {

        ApiResponse<String> response = new ApiResponse<>(
                false,
                "Forbidden",
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
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

    @ExceptionHandler(ServiceRequestNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleServiceRequestNotFound(
            ServiceRequestNotFoundException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(InvalidRequestStateException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidRequestState(
            InvalidRequestStateException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(UnauthorizedRequestActionException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedRequestAction(
            UnauthorizedRequestActionException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }
}
