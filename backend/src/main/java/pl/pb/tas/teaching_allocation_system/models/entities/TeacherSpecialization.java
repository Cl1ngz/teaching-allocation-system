package pl.pb.tas.teaching_allocation_system.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Competence of a teacher in a specialization (M:N with an attribute). The key
 * is composite; both parts come from the associations via @MapsId. The table
 * has no technical columns, so this entity does not extend BaseEntity.
 */
@Entity
@Table(name = "teacher_specializations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeacherSpecialization {

	@EmbeddedId
	private TeacherSpecializationId id;

	@MapsId("teacherId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id")
	private Teacher teacher;

	@MapsId("specializationId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "specialization_id")
	private Specialization specialization;

	/** Optional competence level 1-3. */
	@Setter
	@Column(name = "level")
	private Short level;

	public TeacherSpecialization(Teacher teacher, Specialization specialization, Short level) {
		this.id = new TeacherSpecializationId(teacher.getId(), specialization.getId());
		this.teacher = teacher;
		this.specialization = specialization;
		this.level = level;
	}
}
