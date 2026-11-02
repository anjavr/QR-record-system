package com.evidencija.evidencija_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UploadConflictException.class)
    public ResponseEntity<?> handleUploadConflict(UploadConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getConflict());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", e.getStatusCode().value());
        body.put("error", e.getStatusCode().toString());
        body.put("message", e.getReason());

        return ResponseEntity.status(e.getStatusCode()).body(body);
    }
}