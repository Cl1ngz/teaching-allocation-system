package pl.pb.tas.teaching_allocation_system.models.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Composite primary key of course_programs: (course_id, study_program_id). */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CourseProgramId implements Serializable {

	@Column(name = "course_id")
	private Long courseId;

	@Column(name = "study_program_id")
	private Long studyProgramId;
}
