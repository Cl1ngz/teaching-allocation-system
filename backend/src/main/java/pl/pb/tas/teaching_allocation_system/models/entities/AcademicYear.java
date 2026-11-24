package pl.pb.tas.teaching_allocation_system.models.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Academic year, e.g. 2026/2027. The current year is not stored: it is the one
 * whose dates contain today (see AcademicYearRepository.findContaining).
 */
@Entity
@Table(name = "academic_years")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicYear extends BaseEntity {

	@Column(name = "label", nullable = false, unique = true, length = 9)
	private String label;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	public AcademicYear(String label, LocalDate startDate, LocalDate endDate) {
		this.label = label;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public boolean contains(LocalDate date) {
		return !date.isBefore(startDate) && !date.isAfter(endDate);
	}
}
