package pl.pb.tas.teaching_allocation_system.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.pb.tas.teaching_allocation_system.models.HelloResponse;

/**
 * Controller providing a basic diagnostic endpoint to verify backend operation
 * and HTTP communication.
 */
@RestController
@RequestMapping("/api/v1")
public class HelloController {

    private static final String HELLO_MESSAGE = "Teaching Allocation System API";

    /**
     * Handles GET /api/hello request.
     *
     * @return 200 OK with {@link HelloResponse} in JSON format
     */
    @GetMapping("/hello")
    public ResponseEntity<HelloResponse> hello() {
        return ResponseEntity.ok(new HelloResponse(HELLO_MESSAGE));
    }
}
