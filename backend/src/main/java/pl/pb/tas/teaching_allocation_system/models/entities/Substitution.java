package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;
import java.time.Instant;
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
import pl.pb.tas.teaching_allocation_system.models.enums.ApprovalStatus;

/**
 * Temporary substitution: hours taken over from an assignment by another
 * teacher. The assignment determines the group and the substituted teacher.
 */
@Entity
@Table(name = "substitutions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Substitution extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "assignment_id", nullable = false)
	private TeachingAssignment assignment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "substitute_teacher_id", nullable = false)
	private Teacher substituteTeacher;

	@Column(name = "date_from", nullable = false)
	private LocalDate dateFrom;

	@Column(name = "date_to", nullable = false)
	private LocalDate dateTo;

	@Column(name = "hours", nullable = false, precision = 6, scale = 2)
	private BigDecimal hours;

	@Column(name = "reason")
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ApprovalStatus status = ApprovalStatus.SUBMITTED;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "requested_by", nullable = false)
	private AppUser requestedBy;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "decided_by")
	private AppUser decidedBy;

	@Column(name = "decided_at")
	private Instant decidedAt;

	public Substitution(TeachingAssignment assignment, Teacher substituteTeacher,
			LocalDate dateFrom, LocalDate dateTo, BigDecimal hours, AppUser requestedBy) {
		this.assignment = assignment;
		this.substituteTeacher = substituteTeacher;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.hours = hours;
		this.requestedBy = requestedBy;
		this.requestedAt = Instant.now();
	}
}
