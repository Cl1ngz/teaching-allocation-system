package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.DutyType;

public interface DutyTypeRepository extends JpaRepository<DutyType, Long> {

	Optional<DutyType> findByCode(String code);
}
