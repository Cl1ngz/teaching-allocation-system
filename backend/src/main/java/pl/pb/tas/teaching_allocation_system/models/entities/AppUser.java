package pl.pb.tas.teaching_allocation_system.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Application account, optionally linked to one teacher. For linked accounts
 * teachers.email is the authoritative address (data model, Q15).
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "teacher_id", unique = true)
	private Teacher teacher;

	@Column(name = "username", nullable = false, unique = true, length = 100)
	private String username;

	@Column(name = "email")
	private String email;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	public AppUser(String username) {
		this.username = username;
	}
}
