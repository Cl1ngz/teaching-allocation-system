package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Yearly pensum snapshot of a teacher (deviation O1 of the data model).
 * effectiveHours is computed by the database (base - reduction) and read back
 * after every insert and update; it is never written by the application.
 */
@Entity
@Table(name = "teacher_pensums")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeacherPensum extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYear academicYear;

	@Column(name = "base_hours", nullable = false, precision = 6, scale = 2)
	private BigDecimal baseHours;

	@Column(name = "reduction_hours", nullable = false, precision = 6, scale = 2)
	private BigDecimal reductionHours;

	@Setter(AccessLevel.NONE)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(name = "effective_hours", insertable = false, updatable = false, precision = 6, scale = 2)
	private BigDecimal effectiveHours;

	@Column(name = "calculated_at", nullable = false)
	private Instant calculatedAt;

	public TeacherPensum(Teacher teacher, AcademicYear academicYear, BigDecimal baseHours, BigDecimal reductionHours) {
		this.teacher = teacher;
		this.academicYear = academicYear;
		this.baseHours = baseHours;
		this.reductionHours = reductionHours;
		this.calculatedAt = Instant.now();
	}
}
