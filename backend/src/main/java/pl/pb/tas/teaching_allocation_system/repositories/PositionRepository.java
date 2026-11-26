package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {

	Optional<Position> findByCode(String code);

	List<Position> findAllByOrderByNameAsc();
}
