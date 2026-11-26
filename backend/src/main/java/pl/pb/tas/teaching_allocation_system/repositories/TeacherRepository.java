package pl.pb.tas.teaching_allocation_system.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pl.pb.tas.teaching_allocation_system.models.TeacherSummary;
import pl.pb.tas.teaching_allocation_system.models.entities.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

	/**
	 * Teacher list for the given day in a single SQL query: degree plus the
	 * PRIMARY employment in force on that day (left join, so teachers without
	 * one are included with empty unit and position).
	 */
	@Query("""
			select new pl.pb.tas.teaching_allocation_system.models.TeacherSummary(
				t.id, t.firstName, t.lastName, t.email, t.active,
				d.code, d.name,
				u.id, u.name, p.name)
			from Teacher t
				join t.academicDegree d
				left join Employment e
					on e.teacher = t
					and e.employmentType = pl.pb.tas.teaching_allocation_system.models.enums.EmploymentType.PRIMARY
					and e.validFrom <= :date
					and (e.validTo is null or e.validTo >= :date)
				left join e.unit u
				left join e.position p
			order by t.lastName, t.firstName, t.id
			""")
	List<TeacherSummary> findAllSummaries(@Param("date") LocalDate date);

	@Query("""
			select new pl.pb.tas.teaching_allocation_system.models.TeacherSummary(
				t.id, t.firstName, t.lastName, t.email, t.active,
				d.code, d.name,
				u.id, u.name, p.name)
			from Teacher t
				join t.academicDegree d
				left join Employment e
					on e.teacher = t
					and e.employmentType = pl.pb.tas.teaching_allocation_system.models.enums.EmploymentType.PRIMARY
					and e.validFrom <= :date
					and (e.validTo is null or e.validTo >= :date)
					and e.id = (
						select min(e2.id)
						from Employment e2
						where e2.teacher = t
							and e2.employmentType = pl.pb.tas.teaching_allocation_system.models.enums.EmploymentType.PRIMARY
							and e2.validFrom <= :date
							and (e2.validTo is null or e2.validTo >= :date)
					)
				left join e.unit u
				left join e.position p
			""")
	Page<TeacherSummary> findSummaries(@Param("date") LocalDate date, Pageable pageable);

	/** Teacher entities with the degree loaded in the same query. */
	@EntityGraph(attributePaths = "academicDegree")
	List<Teacher> findAllByOrderByLastNameAscFirstNameAsc();
}
