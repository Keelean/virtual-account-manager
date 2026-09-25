package com.keelean.accountmanager.exception;

import com.keelean.accountmanager.config.ApplicationProperties;
import com.keelean.accountmanager.dto.response.Response;
import com.keelean.accountmanager.dto.response.ValidationError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps exceptions to HTTP statuses and the standard {@link Response} body.
 * Client errors return 4xx; downstream and unexpected failures stay 5xx.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String MESSAGE_KEY_SUFFIX = ".ERROR";

    private final MessageSource messageSource;
    private final ApplicationProperties applicationProperties;

    @ExceptionHandler(RestServiceException.class)
    public ResponseEntity<Object> handleRestServiceException(RestServiceException ex) {
        String code = ex.getMessage();
        HttpStatus status = ErrorCodes.fromCode(code).map(ErrorCodes::getStatus).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        String message = messageSource.getMessage(code + MESSAGE_KEY_SUFFIX, ex.getArgs(), code, LocaleContextHolder.getLocale());
        return failure(status, code, message, ex, null);
    }

    // Business rule violations such as an exhausted pool, a full shared pool or an unknown prefix series
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return failure(HttpStatus.BAD_REQUEST, String.valueOf(HttpStatus.BAD_REQUEST.value()), ex.getMessage(), ex, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return failure(status, String.valueOf(status.value()), status.getReasonPhrase(), ex, null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        return validationFailure(ex.getBindingResult(), ex);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return validationFailure(ex.getBindingResult(), ex);
    }

    // Every other Spring MVC exception (unreadable body, type mismatch, unsupported method, ...) keeps its status
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        ResponseEntity<Object> response = failure(status, String.valueOf(status.value()), status.getReasonPhrase(), ex, null);
        return new ResponseEntity<>(response.getBody(), headers, status);
    }

    private ResponseEntity<Object> validationFailure(BindingResult bindingResult, Exception ex) {
        List<ValidationError> errors = bindingResult.getAllErrors().stream()
                .map(error -> ValidationError.builder()
                        .path(error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName())
                        .code(error.getCode())
                        .msg(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        String code = ErrorCodes.VALIDATION_FAILED.getCode();
        String fields = errors.stream().map(ValidationError::getPath).distinct().collect(Collectors.joining(", "));
        String message = messageSource.getMessage(code + MESSAGE_KEY_SUFFIX, new Object[]{fields}, code, LocaleContextHolder.getLocale());
        return failure(HttpStatus.BAD_REQUEST, code, message, ex, errors);
    }

    private ResponseEntity<Object> failure(HttpStatus status, String code, String message, Exception ex,
                                           @Nullable Collection<ValidationError> validationErrors) {
        if (status.is5xxServerError()) {
            log.error("Request failed with {} ({})", status.value(), code, ex);
        } else {
            log.info("Request rejected with {} ({}): {}", status.value(), code, ex.getMessage());
        }
        Response<Object> body = new Response<>();
        body.setFailureResponse(code, message, ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                applicationProperties.isInjectDevErrorMessage(), validationErrors);
        return ResponseEntity.status(status).body(body);
    }
}
