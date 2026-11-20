package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.OrganizationalUnit;

public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, Long> {

	/** Top level of the hierarchy (faculties). */
	List<OrganizationalUnit> findByParentIsNullOrderByNameAsc();

	/** Direct children of a unit. */
	List<OrganizationalUnit> findByParentIdOrderByNameAsc(Long parentId);
}
