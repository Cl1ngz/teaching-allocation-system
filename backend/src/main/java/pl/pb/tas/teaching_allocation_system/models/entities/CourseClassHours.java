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

/** Planned hours of a course for one class type (one row per type). */
@Entity
@Table(name = "course_class_hours")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseClassHours extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_type_id", nullable = false)
	private ClassType classType;

	@Column(name = "hours", nullable = false, precision = 6, scale = 2)
	private BigDecimal hours;

	public CourseClassHours(Course course, ClassType classType, BigDecimal hours) {
		this.course = course;
		this.classType = classType;
		this.hours = hours;
	}
}
