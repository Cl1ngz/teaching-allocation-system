package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;

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
 * Additional duty of a teacher in an academic year with its pensum reduction.
 * reductionHours starts from DutyType.defaultReductionHours but may differ.
 */
@Entity
@Table(name = "teacher_duties")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeacherDuty extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYear academicYear;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "duty_type_id", nullable = false)
	private DutyType dutyType;

	@Column(name = "reduction_hours", nullable = false, precision = 6, scale = 2)
	private BigDecimal reductionHours;

	@Column(name = "note")
	private String note;

	public TeacherDuty(Teacher teacher, AcademicYear academicYear, DutyType dutyType, BigDecimal reductionHours) {
		this.teacher = teacher;
		this.academicYear = academicYear;
		this.dutyType = dutyType;
		this.reductionHours = reductionHours;
	}
}
