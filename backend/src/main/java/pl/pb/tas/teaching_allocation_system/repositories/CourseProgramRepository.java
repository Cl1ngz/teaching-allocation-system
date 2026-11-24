package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.CourseProgram;
import pl.pb.tas.teaching_allocation_system.models.entities.CourseProgramId;

public interface CourseProgramRepository extends JpaRepository<CourseProgram, CourseProgramId> {

	List<CourseProgram> findByStudyProgramIdOrderBySemesterNoAsc(Long studyProgramId);
}
