package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.ClassGroup;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long> {

	List<ClassGroup> findByOfferingIdOrderByGroupCodeAsc(Long offeringId);
}
