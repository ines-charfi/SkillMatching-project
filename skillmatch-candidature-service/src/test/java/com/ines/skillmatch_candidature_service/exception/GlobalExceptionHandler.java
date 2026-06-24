package com.ines.skillmatch_candidature_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());

        // Si le message contient "introuvable", on renvoie un 404, sinon un 400
        if (ex.getMessage().toLowerCase().contains("introuvable")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}