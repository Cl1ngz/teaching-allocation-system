package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.Semester;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

	List<Semester> findByAcademicYearIdOrderByStartDateAsc(Long academicYearId);
}
