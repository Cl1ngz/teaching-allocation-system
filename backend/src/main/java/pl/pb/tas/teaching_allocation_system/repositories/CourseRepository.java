package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

	Optional<Course> findByCode(String code);
}
