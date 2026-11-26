package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.TeacherSpecialization;
import pl.pb.tas.teaching_allocation_system.models.entities.TeacherSpecializationId;

public interface TeacherSpecializationRepository extends JpaRepository<TeacherSpecialization, TeacherSpecializationId> {

	List<TeacherSpecialization> findByTeacherId(Long teacherId);
}
