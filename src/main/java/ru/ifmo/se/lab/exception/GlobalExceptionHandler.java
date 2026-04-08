package ru.ifmo.se.lab.exception;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
        private static final String LOGIN_UNIQUE_CONSTRAINT = "app_user_login_key";

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                if (ex.getMessage() != null
                                && ex.getMessage().contains(LOGIN_UNIQUE_CONSTRAINT)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                        ErrorResponse.builder()
                                                        .timestamp(Instant.now())
                                                        .status(HttpStatus.CONFLICT.value())
                                                        .error(HttpStatus.CONFLICT.getReasonPhrase())
                                                        .message("User with this login already exists")
                                                        .path(request.getRequestURI())
                                                        .build());
                }

                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ErrorResponse.builder()
                                                .timestamp(Instant.now())
                                                .status(HttpStatus.CONFLICT.value())
                                                .error(HttpStatus.CONFLICT.getReasonPhrase())
                                                .message("Data integrity violation: " + ex.getMessage())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleIllegalStateException(
                        IllegalStateException e,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ErrorResponse.builder()
                                                .timestamp(Instant.now())
                                                .status(HttpStatus.CONFLICT.value())
                                                .error(HttpStatus.CONFLICT.getReasonPhrase())
                                                .message(e.getMessage())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<FieldValidationError> validationErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(this::toFieldValidationError)
                                .toList();

                return ResponseEntity.badRequest().body(
                                ErrorResponse.builder()
                                                .timestamp(Instant.now())
                                                .status(HttpStatus.BAD_REQUEST.value())
                                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                                .message("Validation failed")
                                                .path(request.getRequestURI())
                                                .validationErrors(validationErrors)
                                                .build());
        }

        @ExceptionHandler({
                        ConstraintViolationException.class,
                        HandlerMethodValidationException.class,
                        IllegalArgumentException.class
        })
        public ResponseEntity<ErrorResponse> handleBadRequest(
                        Exception ex,
                        HttpServletRequest request) {
                return ResponseEntity.badRequest().body(
                                ErrorResponse.builder()
                                                .timestamp(Instant.now())
                                                .status(HttpStatus.BAD_REQUEST.value())
                                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                                .message(ex.getMessage())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ErrorResponse.builder()
                                                .timestamp(Instant.now())
                                                .status(HttpStatus.NOT_FOUND.value())
                                                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                                                .message(ex.getMessage())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponse> handleConflict(
                        ConflictException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ErrorResponse.builder()
                                                .timestamp(Instant.now())
                                                .status(HttpStatus.CONFLICT.value())
                                                .error(HttpStatus.CONFLICT.getReasonPhrase())
                                                .message(ex.getMessage())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleInternalServerError(
                        Exception ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                ErrorResponse.builder()
                                                .timestamp(Instant.now())
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                                                .message("Unexpected internal server error")
                                                .path(request.getRequestURI())
                                                .build());
        }

        private FieldValidationError toFieldValidationError(FieldError error) {
                return FieldValidationError.builder()
                                .field(error.getField())
                                .rejectedValue(error.getRejectedValue())
                                .message(error.getDefaultMessage())
                                .build();
        }

        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                        org.springframework.security.access.AccessDeniedException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ErrorResponse.builder()
                                                .timestamp(Instant.now())
                                                .status(HttpStatus.FORBIDDEN.value())
                                                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                                                .message(ex.getMessage())
                                                .path(request.getRequestURI())
                                                .build());
        }
}