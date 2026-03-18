package com.shecancode.attendence.registration.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.apache.commons.text.StringEscapeUtils;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        String sanitizedMessage = StringEscapeUtils.escapeHtml4(ex.getMessage());
        String sanitizedUri = StringEscapeUtils.escapeHtml4(request.getRequestURI());
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not found",
                sanitizedMessage,
                sanitizedUri);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String sanitizedMessage = StringEscapeUtils.escapeHtml4(ex.getMessage());
        String sanitizedUri = StringEscapeUtils.escapeHtml4(request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                sanitizedMessage,
                sanitizedUri);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
