package pl.pb.tas.teaching_allocation_system.models.entities;

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
import pl.pb.tas.teaching_allocation_system.models.enums.Term;

/** Winter or summer semester of an academic year. */
@Entity
@Table(name = "semesters")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Semester extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYear academicYear;

	@Enumerated(EnumType.STRING)
	@Column(name = "term", nullable = false, length = 10)
	private Term term;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	public Semester(AcademicYear academicYear, Term term, LocalDate startDate, LocalDate endDate) {
		this.academicYear = academicYear;
		this.term = term;
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
