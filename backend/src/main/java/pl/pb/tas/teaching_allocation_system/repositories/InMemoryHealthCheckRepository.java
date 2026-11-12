package pl.pb.tas.teaching_allocation_system.repositories;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryHealthCheckRepository implements HealthCheckRepository {

	@Override
	public String getStatus() {
		return "UP";
	}
}
