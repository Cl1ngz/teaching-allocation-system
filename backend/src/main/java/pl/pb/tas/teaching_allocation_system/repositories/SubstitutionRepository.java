package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.Substitution;

public interface SubstitutionRepository extends JpaRepository<Substitution, Long> {

	List<Substitution> findByAssignmentId(Long assignmentId);
}
