package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.Specialization;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

	Optional<Specialization> findByCode(String code);
}
