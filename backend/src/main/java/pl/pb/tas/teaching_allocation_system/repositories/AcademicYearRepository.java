package pl.pb.tas.teaching_allocation_system.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pl.pb.tas.teaching_allocation_system.models.entities.AcademicYear;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

	Optional<AcademicYear> findByLabel(String label);

	/** Academic year containing the given day; with today's date this is the current year (Z1). */
	@Query("select y from AcademicYear y where y.startDate <= :date and y.endDate >= :date")
	Optional<AcademicYear> findContaining(@Param("date") LocalDate date);
}
