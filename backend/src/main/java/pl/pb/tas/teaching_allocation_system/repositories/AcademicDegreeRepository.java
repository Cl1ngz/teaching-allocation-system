package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.AcademicDegree;

public interface AcademicDegreeRepository extends JpaRepository<AcademicDegree, Long> {

	Optional<AcademicDegree> findByCode(String code);

	List<AcademicDegree> findAllByOrderByRankOrderAsc();
}
