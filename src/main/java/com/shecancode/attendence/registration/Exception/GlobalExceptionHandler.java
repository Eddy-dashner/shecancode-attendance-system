package com.shecancode.attendence.registration.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.mail.MailException;
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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid username or password.",
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

    // ── 400: activation token & cohort/program integrity failures ──
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

    // ── 404: unhandled domain "not found" exceptions (previously fell through to 500) ──
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

    // ── 409: duplicate / state-conflict exceptions (previously fell through to 500) ──
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

    // ── 502: email delivery failed. Never leak SMTP internals to the client. ──
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorResponse> handleMailException(MailException ex, HttpServletRequest request) {
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
