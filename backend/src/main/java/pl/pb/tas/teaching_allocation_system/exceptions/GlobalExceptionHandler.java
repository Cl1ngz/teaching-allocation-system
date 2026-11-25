package pl.pb.tas.teaching_allocation_system.exceptions;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import jakarta.validation.ConstraintViolationException;

/**
 * Central exception handling. Every error is returned as an RFC 9457 Problem Details
 * object (application/problem+json) extended with: code, timestamp, errorId and,
 * for validation errors, errors[].
 *
 * <p>Framework exceptions (unknown URL, wrong method, unreadable body, ...) are handled by
 * {@link ResponseEntityExceptionHandler} and converted to the same format in
 * {@link #handleExceptionInternal}. Responses never contain exception messages from the
 * framework, stack traces or database details; those go only to the log, under the errorId.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String CODE = "code";
    static final String TIMESTAMP = "timestamp";
    static final String ERROR_ID = "errorId";
    static final String ERRORS = "errors";

    private final Clock clock;

    // ObjectProvider: the handler also works in @WebMvcTest slices where the Clock bean is absent.
    public GlobalExceptionHandler(ObjectProvider<Clock> clock) {
        this.clock = clock.getIfAvailable(Clock::systemUTC);
    }

    /** Business errors thrown by the application code. */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(ApplicationException ex, WebRequest request) {
        ErrorCode code = ex.getErrorCode();
        String errorId = newErrorId();
        logIssue(errorId, code, code.getStatus(), ex, request);
        String message = code == ErrorCode.VALIDATION_ERROR ? code.getDefaultMessage() : ex.getMessage();
        return respond(problem(code, code.getStatus(), message, List.of(), errorId, request));
    }

    /** Database unavailable (no connection, cannot start a transaction). */
    @ExceptionHandler({ DataAccessResourceFailureException.class, CannotCreateTransactionException.class })
    public ResponseEntity<Object> handleDatabaseUnavailable(Exception ex, WebRequest request) {
        return respondWithDefault(ErrorCode.SERVICE_UNAVAILABLE, ex, request);
    }

    /** Constraint violations and concurrent modifications. */
    @ExceptionHandler({ DataIntegrityViolationException.class, OptimisticLockingFailureException.class })
    public ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        return respondWithDefault(ErrorCode.CONFLICT, ex, request);
    }

    /** Everything else: 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) throws Exception {
        // Security exceptions are not ours to handle (401/403 are out of scope of this format for now).
        if (ex instanceof AccessDeniedException || ex instanceof AuthenticationException) {
            throw ex;
        }
        return respondWithDefault(ErrorCode.INTERNAL_ERROR, ex, request);
    }

    /** @Valid on a request body: adds the list of invalid fields. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ApiFieldError> fields = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fields.add(new ApiFieldError(error.getField(), error.getCode(), error.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                fields.add(new ApiFieldError(error.getObjectName(), error.getCode(), error.getDefaultMessage())));

        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        String errorId = newErrorId();
        logIssue(errorId, code, code.getStatus(), ex, request);
        ProblemDetail problem = problem(code, code.getStatus(), code.getDefaultMessage(), fields, errorId, request);
        return super.handleExceptionInternal(ex, problem, headers, code.getStatus(), request);
    }

    /** Constraint violations on @Validated controller method parameters. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ApiFieldError> fields = ex.getConstraintViolations().stream()
                .map(violation -> new ApiFieldError(
                        violation.getPropertyPath().toString(),
                        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        violation.getMessage()))
                .toList();
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        String errorId = newErrorId();
        logIssue(errorId, code, code.getStatus(), ex, request);
        return respond(problem(code, code.getStatus(), code.getDefaultMessage(), fields, errorId, request));
    }

    /** All other framework exceptions (404, 405, unreadable body, wrong parameter type, ...). */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        // Already converted to our format (see handleMethodArgumentNotValid).
        if (body instanceof ProblemDetail existing
                && existing.getProperties() != null
                && existing.getProperties().containsKey(ERROR_ID)) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }

        ErrorCode code = codeFor(ex, statusCode);
        String errorId = newErrorId();
        logIssue(errorId, code, statusCode, ex, request);
        ProblemDetail problem = problem(code, statusCode, code.getDefaultMessage(), List.of(), errorId, request);
        return super.handleExceptionInternal(ex, problem, headers, statusCode, request);
    }

    private static ErrorCode codeFor(Exception ex, HttpStatusCode status) {
        if (ex instanceof HandlerMethodValidationException) {
            return ErrorCode.VALIDATION_ERROR;
        }
        return switch (status.value()) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 409 -> ErrorCode.CONFLICT;
            case 503 -> ErrorCode.SERVICE_UNAVAILABLE;
            default -> status.is5xxServerError() ? ErrorCode.INTERNAL_ERROR : ErrorCode.MALFORMED_REQUEST;
        };
    }

    private ResponseEntity<Object> respondWithDefault(ErrorCode code, Exception ex, WebRequest request) {
        String errorId = newErrorId();
        logIssue(errorId, code, code.getStatus(), ex, request);
        return respond(problem(code, code.getStatus(), code.getDefaultMessage(), List.of(), errorId, request));
    }

    private static ResponseEntity<Object> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private ProblemDetail problem(ErrorCode code, HttpStatusCode status, String message,
            List<ApiFieldError> fields, String errorId, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            problem.setInstance(URI.create(description.substring("uri=".length())));
        }
        problem.setProperty(CODE, code.name());
        problem.setProperty(TIMESTAMP, Instant.now(clock).toString());
        problem.setProperty(ERROR_ID, errorId);
        if (!fields.isEmpty()) {
            problem.setProperty(ERRORS, fields);
        }
        return problem;
    }

    /** Logs the details (with the stack trace for 5xx) so the errorId from the response can be found in the logs. */
    private void logIssue(String errorId, ErrorCode code, HttpStatusCode status, Exception ex, WebRequest request) {
        String path = request.getDescription(false);
        if (status.is5xxServerError()) {
            log.error("errorId={} code={} {}", errorId, code, path, ex);
        } else {
            log.warn("errorId={} code={} {} cause={}: {}", errorId, code, path,
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    private static String newErrorId() {
        return UUID.randomUUID().toString();
    }
}