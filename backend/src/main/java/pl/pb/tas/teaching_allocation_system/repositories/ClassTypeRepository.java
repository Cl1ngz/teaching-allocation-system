package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.ClassType;

public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {

	Optional<ClassType> findByCode(String code);
}
