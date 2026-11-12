package pl.pb.tas.teaching_allocation_system.services;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;

import pl.pb.tas.teaching_allocation_system.models.HealthResponse;
import pl.pb.tas.teaching_allocation_system.repositories.HealthCheckRepository;

@Service
public class HealthCheckService {

	private final HealthCheckRepository healthCheckRepository;
	private final Clock clock;

	public HealthCheckService(HealthCheckRepository healthCheckRepository, Clock clock) {
		this.healthCheckRepository = healthCheckRepository;
		this.clock = clock;
	}

	public HealthResponse getHealth() {
		return new HealthResponse(
				healthCheckRepository.getStatus(),
				Instant.now(clock));
	}
}
