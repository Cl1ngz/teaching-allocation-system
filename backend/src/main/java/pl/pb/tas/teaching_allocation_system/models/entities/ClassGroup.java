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
 * One class type in an offered course (e.g. lab group L1); the unit teachers
 * are assigned to. hours starts from CourseClassHours but may differ per group.
 */
@Entity
@Table(name = "class_groups")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassGroup extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "offering_id", nullable = false)
	private CourseOffering offering;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_type_id", nullable = false)
	private ClassType classType;

	@Column(name = "group_code", nullable = false, length = 30)
	private String groupCode;

	@Column(name = "hours", nullable = false, precision = 6, scale = 2)
	private BigDecimal hours;

	@Column(name = "student_count")
	private Integer studentCount;

	@Column(name = "usos_id", unique = true, length = 50)
	private String usosId;

	public ClassGroup(CourseOffering offering, ClassType classType, String groupCode, BigDecimal hours) {
		this.offering = offering;
		this.classType = classType;
		this.groupCode = groupCode;
		this.hours = hours;
	}
}
