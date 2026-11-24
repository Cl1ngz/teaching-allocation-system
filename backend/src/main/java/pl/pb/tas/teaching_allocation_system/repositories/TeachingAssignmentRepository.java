package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.TeachingAssignment;
import pl.pb.tas.teaching_allocation_system.models.enums.AssignmentStatus;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

	/** Assignments of a group with their teachers loaded in the same query. */
	@EntityGraph(attributePaths = "teacher")
	List<TeachingAssignment> findByClassGroupId(Long classGroupId);

	List<TeachingAssignment> findByTeacherIdAndStatus(Long teacherId, AssignmentStatus status);
}
