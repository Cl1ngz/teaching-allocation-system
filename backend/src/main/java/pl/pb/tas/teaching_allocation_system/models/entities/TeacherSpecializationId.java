package pl.pb.tas.teaching_allocation_system.models.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Composite primary key of teacher_specializations: (teacher_id, specialization_id). */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeacherSpecializationId implements Serializable {

	@Column(name = "teacher_id")
	private Long teacherId;

	@Column(name = "specialization_id")
	private Long specializationId;
}
