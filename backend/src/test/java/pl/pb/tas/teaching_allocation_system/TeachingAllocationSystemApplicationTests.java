package pl.pb.tas.teaching_allocation_system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// The "test" profile keeps the database auto-configuration disabled, so this test
// does not require a running PostgreSQL instance. Database integration tests
// are planned in SF-12.
@SpringBootTest
@ActiveProfiles("test")
class TeachingAllocationSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
