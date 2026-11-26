package pl.pb.tas.teaching_allocation_system.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pl.pb.tas.teaching_allocation_system.exceptions.ApiErrorResponse;
import pl.pb.tas.teaching_allocation_system.models.PageResponse;
import pl.pb.tas.teaching_allocation_system.models.TeacherListItem;
import pl.pb.tas.teaching_allocation_system.services.TeacherService;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Teachers", description = "Academic teacher information")
public class TeacherController {

	private final TeacherService teacherService;

	public TeacherController(TeacherService teacherService) {
		this.teacherService = teacherService;
	}

	@GetMapping("/teachers")
	@Operation(
			summary = "List academic teachers",
			description = "Returns a paginated list with the active primary employment.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Paginated teacher list",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = PageResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid pagination/sorting parameter or malformed request (VALIDATION_ERROR, MALFORMED_REQUEST)",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class),
							examples = {
									@ExampleObject(name = "VALIDATION_ERROR", summary = "Invalid pagination or sort parameter", value = """
											{
											  "type": "about:blank",
											  "title": "Bad Request",
											  "status": 400,
											  "detail": "Request validation failed.",
											  "instance": "/api/v1/teachers",
											  "code": "VALIDATION_ERROR",
											  "timestamp": "2026-11-25T16:30:00Z",
											  "errorId": "550e8400-e29b-41d4-a716-446655440000",
											  "errors": [
											    {
											      "field": "listTeachers.page",
											      "code": "Min",
											      "message": "must be greater than or equal to 0"
											    }
											  ]
											}
											"""),
									@ExampleObject(name = "MALFORMED_REQUEST", summary = "Wrong parameter type (e.g. page=abc)", value = """
											{
											  "type": "about:blank",
											  "title": "Bad Request",
											  "status": 400,
											  "detail": "The request is malformed or unreadable.",
											  "instance": "/api/v1/teachers",
											  "code": "MALFORMED_REQUEST",
											  "timestamp": "2026-11-25T16:30:05Z",
											  "errorId": "6f9619ff-8b86-d011-b42d-00cf4fc964ff"
											}
											""")
							})),
			@ApiResponse(responseCode = "404", description = "The endpoint does not exist (NOT_FOUND)",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class),
							examples = @ExampleObject(value = """
									{
									  "type": "about:blank",
									  "title": "Not Found",
									  "status": 404,
									  "detail": "The requested resource was not found.",
									  "instance": "/api/v1/unknown",
									  "code": "NOT_FOUND",
									  "timestamp": "2026-11-25T16:30:10Z",
									  "errorId": "3f2504e0-4f89-11d3-9a0c-0305e82c3301"
									}
									"""))),
			@ApiResponse(responseCode = "405", description = "The HTTP method is not allowed (METHOD_NOT_ALLOWED)",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class),
							examples = @ExampleObject(value = """
									{
									  "type": "about:blank",
									  "title": "Method Not Allowed",
									  "status": 405,
									  "detail": "The HTTP method is not allowed for this resource.",
									  "instance": "/api/v1/teachers",
									  "code": "METHOD_NOT_ALLOWED",
									  "timestamp": "2026-11-25T16:30:15Z",
									  "errorId": "c9bf9e57-1685-4c89-bafb-ff5af830be8a"
									}
									"""))),
			@ApiResponse(responseCode = "503", description = "Database unavailable (SERVICE_UNAVAILABLE)",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class),
							examples = @ExampleObject(value = """
									{
									  "type": "about:blank",
									  "title": "Service Unavailable",
									  "status": 503,
									  "detail": "The service is temporarily unavailable. Try again later.",
									  "instance": "/api/v1/teachers",
									  "code": "SERVICE_UNAVAILABLE",
									  "timestamp": "2026-11-25T16:30:25Z",
									  "errorId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
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
									  "instance": "/api/v1/teachers",
									  "code": "INTERNAL_ERROR",
									  "timestamp": "2026-11-25T16:30:30Z",
									  "errorId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
									}
									""")))
	})
	public ResponseEntity<PageResponse<TeacherListItem>> listTeachers(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(required = false) @Min(1) @Max(100) Integer size,
			@Parameter(
					description = "Sort fields: id, firstName, lastName, email, active, degreeCode, unitName, positionName. Format: field or field,asc|desc.",
					example = "lastName,asc")
			@RequestParam(required = false) String[] sort) {
		return ResponseEntity.ok(teacherService.getTeachers(page, size, sort));
	}
}
