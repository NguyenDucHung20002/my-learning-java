package com.my.demo.exception;

import com.my.demo.exception.errors.EmailAlreadyExistsException;
import com.my.demo.exception.errors.InvalidInputTypeException;
import com.my.demo.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ApiResponse<Void> response = ApiResponse.error(409, ex.getMessage(), "EMAIL_ALREADY_EXISTS");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(Exception ex) {
        ApiResponse<Void> response = ApiResponse.error(400, "Bad Credentials", "AUTH_FAILED");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse<Map<String, String>> response = ApiResponse.error(400, "Validation failed", "VALIDATION_ERROR");
        response.setData(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidInputTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidInputTypeException(InvalidInputTypeException ex) {
        ApiResponse<Void> response = ApiResponse.error(400, ex.getMessage(), "INVALID_INPUT_TYPE");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage().contains("Refresh token was expired")) {
            ApiResponse<Void> response = ApiResponse.error(403, "Refresh token expired", "REFRESH_TOKEN_EXPIRED");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        ApiResponse<Void> response = ApiResponse.error(500, "Internal server error", "INTERNAL_ERROR");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> response = ApiResponse.error(500, "An unexpected error occurred", "UNKNOWN_ERROR");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
