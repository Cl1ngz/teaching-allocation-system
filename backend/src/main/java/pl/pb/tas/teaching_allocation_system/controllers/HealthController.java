package pl.pb.tas.teaching_allocation_system.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.pb.tas.teaching_allocation_system.exceptions.ApiErrorResponse;
import pl.pb.tas.teaching_allocation_system.models.HealthResponse;
import pl.pb.tas.teaching_allocation_system.services.HealthCheckService;

@RestController
@Tag(name = "Diagnostics", description = "Diagnostic and health check endpoints")
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/api/v1/health")
    @Operation(
            summary = "Application health check",
            description = "Returns basic application health status (does not check the database connection; use /actuator/health for database status).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application is running",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HealthResponse.class))),
            @ApiResponse(responseCode = "405", description = "HTTP method not allowed (METHOD_NOT_ALLOWED)",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Method Not Allowed",
                                      "status": 405,
                                      "detail": "The HTTP method is not allowed for this resource.",
                                      "instance": "/api/v1/health",
                                      "code": "METHOD_NOT_ALLOWED",
                                      "timestamp": "2026-11-25T16:30:00Z",
                                      "errorId": "550e8400-e29b-41d4-a716-446655440000"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error (INTERNAL_ERROR)",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Internal Server Error",
                                      "status": 500,
                                      "detail": "An unexpected error occurred.",
                                      "instance": "/api/v1/health",
                                      "code": "INTERNAL_ERROR",
                                      "timestamp": "2026-11-25T16:30:00Z",
                                      "errorId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                                    }
                                    """)))
    })
    public HealthResponse health() {
        return healthCheckService.getHealth();
    }
}
