package pl.pb.tas.teaching_allocation_system.exceptions;

/**
 * Application exception carrying an {@link ErrorCode}. Services throw it to signal
 * business errors, e.g. {@code throw new ApplicationException(ErrorCode.NOT_FOUND, "Teacher not found.")}.
 * The message is returned to the client, so it must never contain internal details.
 */
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
