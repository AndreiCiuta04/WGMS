package at.ac.tuwien.sepr.groupphase.backend.endpoint.exceptionhandler;

import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.GoneException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps backend exceptions to client-safe HTTP responses.
 *
 * <p>Detailed exception messages are logged on the backend, but responses sent to
 * clients avoid leaking internal identifiers such as database IDs, emails,
 * membership details, invitation-token state, or implementation details.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String BAD_REQUEST_MESSAGE = "Invalid request.";
    private static final String UNAUTHORIZED_MESSAGE = "Authentication failed.";
    private static final String FORBIDDEN_MESSAGE = "Access denied.";
    private static final String NOT_FOUND_MESSAGE = "Resource not found.";
    private static final String CONFLICT_MESSAGE = "Request conflicts with the current resource state.";
    private static final String GONE_MESSAGE = "Resource is no longer available.";

    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<Object> handleValidation(RuntimeException ex, WebRequest request) {
        logClientSafeWarning(ex);
        return handleExceptionInternal(ex, BAD_REQUEST_MESSAGE, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        logClientSafeWarning(ex);
        return handleExceptionInternal(ex, NOT_FOUND_MESSAGE, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    protected ResponseEntity<Object> handleForbidden(RuntimeException ex, WebRequest request) {
        logClientSafeWarning(ex);
        return handleExceptionInternal(ex, FORBIDDEN_MESSAGE, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = {ConflictException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        logClientSafeWarning(ex);
        return handleExceptionInternal(ex, CONFLICT_MESSAGE, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {GoneException.class})
    protected ResponseEntity<Object> handleGone(RuntimeException ex, WebRequest request) {
        logClientSafeWarning(ex);
        return handleExceptionInternal(ex, GONE_MESSAGE, new HttpHeaders(), HttpStatus.GONE, request);
    }

    @ExceptionHandler(value = {BadCredentialsException.class})
    protected ResponseEntity<Object> handleBadCredentials(RuntimeException ex, WebRequest request) {
        logClientSafeWarning(ex);
        return handleExceptionInternal(ex, UNAUTHORIZED_MESSAGE, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        LOGGER.warn("Validation failed: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getDefaultMessage() == null ? BAD_REQUEST_MESSAGE : err.getDefaultMessage())
            .collect(Collectors.toList());

        body.put("Validation errors", errors);
        return new ResponseEntity<>(body, headers, status);
    }

    private void logClientSafeWarning(RuntimeException ex) {
        LOGGER.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
    }
}
