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
 * Course in a study program (M:N with attributes): semester in the study plan
 * and whether it is mandatory. Composite key, no technical columns.
 */
@Entity
@Table(name = "course_programs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseProgram {

	@EmbeddedId
	private CourseProgramId id;

	@MapsId("courseId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id")
	private Course course;

	@MapsId("studyProgramId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "study_program_id")
	private StudyProgram studyProgram;

	@Setter
	@Column(name = "semester_no", nullable = false)
	private Short semesterNo;

	@Setter
	@Column(name = "is_mandatory", nullable = false)
	private boolean mandatory = true;

	public CourseProgram(Course course, StudyProgram studyProgram, Short semesterNo, boolean mandatory) {
		this.id = new CourseProgramId(course.getId(), studyProgram.getId());
		this.course = course;
		this.studyProgram = studyProgram;
		this.semesterNo = semesterNo;
		this.mandatory = mandatory;
	}
}
