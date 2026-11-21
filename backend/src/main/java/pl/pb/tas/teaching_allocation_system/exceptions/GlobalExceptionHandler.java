package pl.pb.tas.teaching_allocation_system.exceptions;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler({
			IllegalArgumentException.class,
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class,
			ConstraintViolationException.class
	})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleValidationException(Exception exception) {
		String message = exception instanceof ConstraintViolationException violation
				? violation.getConstraintViolations().stream().findFirst()
						.map(error -> "Invalid parameter: " + error.getPropertyPath() + ".")
						.orElse("Invalid request parameters.")
				: exception.getMessage();
		return new ApiError(message == null || message.isBlank()
				? "Invalid request parameters."
				: message);
	}

	@ExceptionHandler(DataAccessException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ApiError handleDatabaseUnavailable(DataAccessException exception) {
		return new ApiError("The database service is unavailable.");
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handleUnexpectedException(Exception exception) {
		return new ApiError("An unexpected error occurred.");
	}
}
