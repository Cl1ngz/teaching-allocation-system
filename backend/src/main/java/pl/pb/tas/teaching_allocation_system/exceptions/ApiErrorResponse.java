package pl.pb.tas.teaching_allocation_system.exceptions;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OpenAPI schema for the RFC 9457 error response returned by the API.
 */
@Schema(description = "RFC 9457 Problem Details response extended with the API error code and correlation ID.")
public record ApiErrorResponse(
        @Schema(description = "URI reference identifying the problem type.", example = "about:blank") String type,
        @Schema(description = "Short, human-readable summary of the problem.") String title,
        @Schema(description = "HTTP status code.") int status,
        @Schema(description = "Safe, human-readable explanation.") String detail,
        @Schema(description = "URI reference identifying this occurrence.") String instance,
        @Schema(description = "Stable machine-readable code from the API error catalog.") ErrorCode code,
        @Schema(description = "UTC timestamp when the error occurred.", example = "2026-11-25T16:30:00Z") Instant timestamp,
        @Schema(description = "Correlation ID also written to the backend logs.", example = "550e8400-e29b-41d4-a716-446655440000") UUID errorId,
        @Schema(description = "Invalid request fields, included only for Bean Validation errors.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<ApiFieldError> errors) {
}
