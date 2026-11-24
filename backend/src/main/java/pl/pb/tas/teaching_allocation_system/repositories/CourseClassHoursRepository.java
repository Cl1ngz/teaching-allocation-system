package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.CourseClassHours;

public interface CourseClassHoursRepository extends JpaRepository<CourseClassHours, Long> {

	List<CourseClassHours> findByCourseId(Long courseId);
}
