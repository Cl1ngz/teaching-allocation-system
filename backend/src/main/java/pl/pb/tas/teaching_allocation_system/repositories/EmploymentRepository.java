package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.Employment;

public interface EmploymentRepository extends JpaRepository<Employment, Long> {

	/** Employment history of a teacher, newest first, with unit and position in the same query. */
	@EntityGraph(attributePaths = { "unit", "position" })
	List<Employment> findByTeacherIdOrderByValidFromDesc(Long teacherId);
}
