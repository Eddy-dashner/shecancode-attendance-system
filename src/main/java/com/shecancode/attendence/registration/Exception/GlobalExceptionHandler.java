package com.shecancode.attendence.registration.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.apache.commons.text.StringEscapeUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

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

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                StringEscapeUtils.escapeHtml4(ex.getMessage()),
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid email or password.",
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledAccount(DisabledException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "This account has been disabled.",
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You do not have permission to perform this action.",
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            ActivationTokenException.class,
            CohortProgramMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestDomain(RuntimeException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                StringEscapeUtils.escapeHtml4(ex.getMessage()),
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // These previously fell through to a generic 500.
    @ExceptionHandler({
            CohortNotFoundException.class,
            ProgramNotFoundException.class,
            StudentNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleDomainNotFound(RuntimeException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not found",
                StringEscapeUtils.escapeHtml4(ex.getMessage()),
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // These previously fell through to a generic 500.
    @ExceptionHandler({
            CohortAlreadyExistException.class,
            EmailAlreadyExistException.class,
            StudentDroppedOutException.class,
            AccountAlreadyActivatedException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                StringEscapeUtils.escapeHtml4(ex.getMessage()),
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Never leak email-provider internals to the client.
    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ErrorResponse> handleMailException(EmailDeliveryException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "Email Delivery Failed",
                "We could not send the email at this time. Please try again later.",
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                message,
                StringEscapeUtils.escapeHtml4(request.getRequestURI()));
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
