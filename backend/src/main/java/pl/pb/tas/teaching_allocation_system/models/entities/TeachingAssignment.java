package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
import pl.pb.tas.teaching_allocation_system.models.enums.AssignmentStatus;

/**
 * Teacher assigned to a class group with a percentage share. Assigned hours
 * are not stored (decision D4), see assignedHours().
 */
@Entity
@Table(name = "teaching_assignments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeachingAssignment extends BaseEntity {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_group_id", nullable = false)
	private ClassGroup classGroup;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@Column(name = "share_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal sharePercent;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private AssignmentStatus status = AssignmentStatus.DRAFT;

	public TeachingAssignment(ClassGroup classGroup, Teacher teacher, BigDecimal sharePercent) {
		this.classGroup = classGroup;
		this.teacher = teacher;
		this.sharePercent = sharePercent;
	}

	/** Group hours x share / 100, in teaching hours, without the class type weight. */
	public BigDecimal assignedHours() {
		return classGroup.getHours().multiply(sharePercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
	}
}
