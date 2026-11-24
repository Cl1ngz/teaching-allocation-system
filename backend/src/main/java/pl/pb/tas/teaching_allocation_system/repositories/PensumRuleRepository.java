package pl.pb.tas.teaching_allocation_system.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pl.pb.tas.teaching_allocation_system.models.entities.PensumRule;
import pl.pb.tas.teaching_allocation_system.models.enums.EmployeeGroup;
import pl.pb.tas.teaching_allocation_system.models.enums.EmploymentType;

public interface PensumRuleRepository extends JpaRepository<PensumRule, Long> {

	/** Rule in force on the given day for a position, staff group and employment type. */
	@Query("""
			select r from PensumRule r
			where r.position.id = :positionId
				and r.employeeGroup = :employeeGroup
				and r.employmentType = :employmentType
				and r.validFrom <= :date
				and (r.validTo is null or r.validTo >= :date)
			""")
	Optional<PensumRule> findInForce(@Param("positionId") Long positionId,
			@Param("employeeGroup") EmployeeGroup employeeGroup,
			@Param("employmentType") EmploymentType employmentType,
			@Param("date") LocalDate date);
}
