package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.TeacherDuty;

public interface TeacherDutyRepository extends JpaRepository<TeacherDuty, Long> {

	List<TeacherDuty> findByTeacherIdAndAcademicYearId(Long teacherId, Long academicYearId);
}
