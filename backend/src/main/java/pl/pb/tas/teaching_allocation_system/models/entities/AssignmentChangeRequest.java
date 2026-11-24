package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;
import java.time.Instant;

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
import pl.pb.tas.teaching_allocation_system.models.enums.ChangeType;

/**
 * Request to change a teaching assignment, with approval. Create it with the
 * factory methods: they fill exactly the fields the database CHECK requires for
 * each change type and copy the current state of the assignment into old*
 * (deviation O2 of the data model). The group of an existing assignment must
 * match classGroup; the database enforces it with a composite foreign key.
 */
@Entity
@Table(name = "assignment_change_requests")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentChangeRequest extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_group_id", nullable = false)
	private ClassGroup classGroup;

	/** null for ADD. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignment_id")
	private TeachingAssignment assignment;

	@Enumerated(EnumType.STRING)
	@Column(name = "change_type", nullable = false, length = 30)
	private ChangeType changeType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "old_teacher_id")
	private Teacher oldTeacher;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "new_teacher_id")
	private Teacher newTeacher;

	@Column(name = "old_share_percent", precision = 5, scale = 2)
	private BigDecimal oldSharePercent;

	@Column(name = "new_share_percent", precision = 5, scale = 2)
	private BigDecimal newSharePercent;

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

	@Column(name = "decision_comment")
	private String decisionComment;

	private AssignmentChangeRequest(ClassGroup classGroup, TeachingAssignment assignment,
			ChangeType changeType, AppUser requestedBy) {
		this.classGroup = classGroup;
		this.assignment = assignment;
		this.changeType = changeType;
		this.requestedBy = requestedBy;
		this.requestedAt = Instant.now();
		if (assignment != null) {
			this.oldTeacher = assignment.getTeacher();
			this.oldSharePercent = assignment.getSharePercent();
		}
	}

	public static AssignmentChangeRequest add(ClassGroup classGroup, Teacher newTeacher,
			BigDecimal newSharePercent, AppUser requestedBy) {
		AssignmentChangeRequest request = new AssignmentChangeRequest(classGroup, null, ChangeType.ADD, requestedBy);
		request.newTeacher = newTeacher;
		request.newSharePercent = newSharePercent;
		return request;
	}

	public static AssignmentChangeRequest remove(TeachingAssignment assignment, AppUser requestedBy) {
		return new AssignmentChangeRequest(assignment.getClassGroup(), assignment, ChangeType.REMOVE, requestedBy);
	}

	public static AssignmentChangeRequest changeShare(TeachingAssignment assignment,
			BigDecimal newSharePercent, AppUser requestedBy) {
		AssignmentChangeRequest request = new AssignmentChangeRequest(
				assignment.getClassGroup(), assignment, ChangeType.CHANGE_SHARE, requestedBy);
		request.newSharePercent = newSharePercent;
		return request;
	}

	public static AssignmentChangeRequest replaceTeacher(TeachingAssignment assignment,
			Teacher newTeacher, AppUser requestedBy) {
		AssignmentChangeRequest request = new AssignmentChangeRequest(
				assignment.getClassGroup(), assignment, ChangeType.REPLACE_TEACHER, requestedBy);
		request.newTeacher = newTeacher;
		return request;
	}
}
