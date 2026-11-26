package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Course in the catalogue, independent of semesters. Required specializations
 * of the teacher are a plain M:N relation (course_specializations has no other
 * columns, so it needs no entity of its own).
 */
@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unit_id", nullable = false)
	private OrganizationalUnit unit;

	/** Minimum degree required from the teacher; null = no requirement. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "min_degree_id")
	private AcademicDegree minDegree;

	@Column(name = "code", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "name_en")
	private String nameEn;

	@Column(name = "ects", nullable = false, precision = 4, scale = 1)
	private BigDecimal ects;

	@Column(name = "usos_id", unique = true, length = 50)
	private String usosId;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Setter(AccessLevel.NONE)
	@ManyToMany
	@JoinTable(name = "course_specializations",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "specialization_id"))
	private Set<Specialization> requiredSpecializations = new HashSet<>();

	public Course(OrganizationalUnit unit, String code, String name, BigDecimal ects) {
		this.unit = unit;
		this.code = code;
		this.name = name;
		this.ects = ects;
	}
}
