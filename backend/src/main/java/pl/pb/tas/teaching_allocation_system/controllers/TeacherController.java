package pl.pb.tas.teaching_allocation_system.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
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
					content = @Content(schema = @Schema(implementation = PageResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameter",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "The endpoint does not exist",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(responseCode = "405", description = "The HTTP method is not allowed",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "Database unavailable",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Unexpected server error",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ApiErrorResponse.class)))
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
