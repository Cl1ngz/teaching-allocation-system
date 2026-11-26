package pl.pb.tas.teaching_allocation_system.exceptions;

import java.time.Clock;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the error format with a minimal MockMvc (no Spring context, no security),
 * using a throw-away controller that triggers each kind of error.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Empty provider: the handler falls back to the system UTC clock.
        GlobalExceptionHandler handler =
                new GlobalExceptionHandler(new StaticListableBeanFactory().getBeanProvider(Clock.class));
        mockMvc = MockMvcBuilders.standaloneSetup(new ErrorTriggerController())
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    @DisplayName("Validation errors list the invalid fields")
    void validationErrorListsFields() throws Exception {
        mockMvc.perform(post("/trigger/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"count\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "count")))
                .andExpect(jsonPath("$.errorId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Unreadable JSON returns MALFORMED_REQUEST")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/trigger/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")))
                .andExpect(jsonPath("$.errorId", notNullValue()));
    }

    @Test
    @DisplayName("Wrong HTTP method returns METHOD_NOT_ALLOWED in the same format")
    void methodNotAllowed() throws Exception {
        mockMvc.perform(post("/trigger/app-error"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code", is("METHOD_NOT_ALLOWED")))
                .andExpect(jsonPath("$.errorId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("ApplicationException uses the status and message of its ErrorCode")
    void applicationException() throws Exception {
        mockMvc.perform(get("/trigger/app-error"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.detail", is("Subject already exists.")));
    }

    @Test
    @DisplayName("Database unavailable returns SERVICE_UNAVAILABLE (503) without connection details")
    void databaseUnavailable() throws Exception {
        mockMvc.perform(get("/trigger/db-down"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code", is("SERVICE_UNAVAILABLE")))
                .andExpect(content().string(not(containsString("jdbc"))));
    }

    @Test
    @DisplayName("Unexpected exception returns INTERNAL_ERROR and hides internal details")
    void unexpectedException() throws Exception {
        mockMvc.perform(get("/trigger/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")))
                .andExpect(jsonPath("$.errorId", notNullValue()))
                .andExpect(content().string(not(containsString("secret_table"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    @Test
    @DisplayName("Unknown URL returns NOT_FOUND in the unified problem format")
    void unknownUrl() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("The requested resource was not found.")))
                .andExpect(jsonPath("$.errorId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Every error code is covered by the explicit error-code contract")
    void everyErrorCodeIsCoveredByTheContract() {
        Set<String> activeCodes = Set.of(
                "VALIDATION_ERROR",
                "MALFORMED_REQUEST",
                "NOT_FOUND",
                "METHOD_NOT_ALLOWED",
                "CONFLICT",
                "SERVICE_UNAVAILABLE",
                "INTERNAL_ERROR",
                "UNAUTHENTICATED",
                "ACCESS_DENIED");

        Set<String> enumCodes = java.util.Arrays.stream(ErrorCode.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertThat(enumCodes).isEqualTo(activeCodes);
    }

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    @DisplayName("Every ErrorCode keeps its HTTP status, code and default problem detail")
    void everyErrorCodeKeepsItsContract(ErrorCode errorCode) throws Exception {
        mockMvc.perform(get("/trigger/error-code/{code}", errorCode.name()))
                .andExpect(status().is(errorCode.getStatus().value()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code", is(errorCode.name())))
                .andExpect(jsonPath("$.status", is(errorCode.getStatus().value())))
                .andExpect(jsonPath("$.detail", is(errorCode.getDefaultMessage())))
                .andExpect(jsonPath("$.errorId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @RestController
    static class ErrorTriggerController {

        record Body(@NotBlank String name, @Min(1) int count) {
        }

        @PostMapping("/trigger/validate")
        void validate(@Valid @RequestBody Body body) {
        }

        @GetMapping("/trigger/app-error")
        void appError() {
            throw new ApplicationException(ErrorCode.CONFLICT, "Subject already exists.");
        }

        @GetMapping("/trigger/error-code/{code}")
        void errorCode(@PathVariable ErrorCode code) {
            throw new ApplicationException(code);
        }

        @GetMapping("/trigger/db-down")
        void dbDown() {
            throw new DataAccessResourceFailureException("Connection to jdbc:postgresql://db:5432/tas refused");
        }

        @GetMapping("/trigger/unexpected")
        void unexpected() {
            throw new IllegalStateException("failure in secret_table");
        }
    }
}
