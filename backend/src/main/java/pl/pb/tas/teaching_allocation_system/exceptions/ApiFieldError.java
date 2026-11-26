package pl.pb.tas.teaching_allocation_system.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single field-level validation error. The rejected value is intentionally not included.
 *
 * @param field   name of the invalid field
 * @param code    validation constraint code (e.g. NotBlank)
 * @param message human-readable description
 */
@Schema(description = "Single field- or parameter-level validation error.")
public record ApiFieldError(
        @Schema(description = "Name of the invalid field or query parameter.", example = "page") String field,
        @Schema(description = "Validation constraint code.", example = "Min") String code,
        @Schema(description = "Readable validation message.", example = "must be greater than or equal to 0") String message) {
}
