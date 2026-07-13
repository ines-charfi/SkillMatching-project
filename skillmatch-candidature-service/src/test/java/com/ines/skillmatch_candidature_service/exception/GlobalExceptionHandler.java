package com.ines.skillmatch_candidature_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the microservice.
 * This class intercepts exceptions thrown by any controller and formats the error response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles RuntimeExceptions and maps them to appropriate HTTP status codes.
     *
     * @param ex The caught RuntimeException.
     * @return A map containing the error message with a 404 or 400 status code.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());

        // Logic to determine the HTTP status based on the exception message content.
        // If the message contains "introuvable" (not found), return 404, otherwise return 400.
        if (ex.getMessage().toLowerCase().contains("introuvable")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}