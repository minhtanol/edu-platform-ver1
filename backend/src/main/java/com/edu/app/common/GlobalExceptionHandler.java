package com.edu.app.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
    var msg = ex.getBindingResult().getFieldErrors().stream().findFirst().map(e -> e.getField() + " " + e.getDefaultMessage()).orElse("Validation error");
    return ResponseEntity.badRequest().body(ApiResponse.error(msg));
  }
  @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
  ResponseEntity<ApiResponse<Void>> badRequest(Exception ex) { return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage())); }
  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiResponse<Void>> denied(Exception ex) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied")); }
  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> server(Exception ex) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal server error")); }
}
