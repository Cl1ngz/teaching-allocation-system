package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.CourseOffering;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {

	Optional<CourseOffering> findByCourseIdAndSemesterId(Long courseId, Long semesterId);

	List<CourseOffering> findBySemesterId(Long semesterId);
}
