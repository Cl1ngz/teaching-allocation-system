package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.TeacherPensum;

public interface TeacherPensumRepository extends JpaRepository<TeacherPensum, Long> {

	Optional<TeacherPensum> findByTeacherIdAndAcademicYearId(Long teacherId, Long academicYearId);
}
