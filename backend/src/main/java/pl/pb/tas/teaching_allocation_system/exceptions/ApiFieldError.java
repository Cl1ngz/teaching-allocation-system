package pl.pb.tas.teaching_allocation_system.exceptions;

/**
 * A single field-level validation error. The rejected value is intentionally not included.
 *
 * @param field   name of the invalid field
 * @param code    validation constraint code (e.g. NotBlank)
 * @param message human-readable description
 */
public record ApiFieldError(String field, String code, String message) {
}
