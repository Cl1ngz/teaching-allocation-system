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
 * Annual pensum for a full-time position, by position, staff group and
 * employment type, valid in a period (validTo = null means still in force).
 */
@Entity
@Table(name = "pensum_rules")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PensumRule extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "position_id", nullable = false)
	private Position position;

	@Enumerated(EnumType.STRING)
	@Column(name = "employee_group", nullable = false, length = 30)
	private EmployeeGroup employeeGroup;

	@Enumerated(EnumType.STRING)
	@Column(name = "employment_type", nullable = false, length = 30)
	private EmploymentType employmentType;

	@Column(name = "annual_hours", nullable = false, precision = 6, scale = 2)
	private BigDecimal annualHours;

	@Column(name = "valid_from", nullable = false)
	private LocalDate validFrom;

	@Column(name = "valid_to")
	private LocalDate validTo;

	public PensumRule(Position position, EmployeeGroup employeeGroup, EmploymentType employmentType,
			BigDecimal annualHours, LocalDate validFrom, LocalDate validTo) {
		this.position = position;
		this.employeeGroup = employeeGroup;
		this.employmentType = employmentType;
		this.annualHours = annualHours;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}
}
