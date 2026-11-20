package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pb.tas.teaching_allocation_system.models.enums.EmployeeGroup;
import pl.pb.tas.teaching_allocation_system.models.enums.EmploymentType;

/**
 * Employment of a teacher in a unit, on a position, in a staff group, over a
 * period of time. validTo = null means an open-ended employment.
 */
@Entity
@Table(name = "employments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unit_id", nullable = false)
	private OrganizationalUnit unit;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "position_id", nullable = false)
	private Position position;

	@Enumerated(EnumType.STRING)
	@Column(name = "employee_group", nullable = false, length = 30)
	private EmployeeGroup employeeGroup;

	@Enumerated(EnumType.STRING)
	@Column(name = "employment_type", nullable = false, length = 30)
	private EmploymentType employmentType;

	@Column(name = "fte", nullable = false, precision = 3, scale = 2)
	private BigDecimal fte;

	@Column(name = "valid_from", nullable = false)
	private LocalDate validFrom;

	@Column(name = "valid_to")
	private LocalDate validTo;

	public Employment(Teacher teacher, OrganizationalUnit unit, Position position,
			EmployeeGroup employeeGroup, EmploymentType employmentType,
			BigDecimal fte, LocalDate validFrom, LocalDate validTo) {
		this.teacher = teacher;
		this.unit = unit;
		this.position = position;
		this.employeeGroup = employeeGroup;
		this.employmentType = employmentType;
		this.fte = fte;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	/** Whether the employment is in force on the given day. */
	public boolean isActiveOn(LocalDate date) {
		return !validFrom.isAfter(date) && (validTo == null || !validTo.isBefore(date));
	}
}
