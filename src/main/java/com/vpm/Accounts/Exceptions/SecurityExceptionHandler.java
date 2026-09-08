package com.vpm.Accounts.Exceptions;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SecurityExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<?> badCredentials() { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("timestamp", Instant.now(), "status", 401, "message", "Invalid username or password")); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException exception) { return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "status", 400, "message", "Request validation failed")); }
}
