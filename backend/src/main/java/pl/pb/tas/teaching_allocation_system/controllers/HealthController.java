package pl.pb.tas.teaching_allocation_system.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.pb.tas.teaching_allocation_system.models.HealthResponse;
import pl.pb.tas.teaching_allocation_system.services.HealthCheckService;

@RestController
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/api/v1/health")
    public HealthResponse health() {
        return healthCheckService.getHealth();
    }
}