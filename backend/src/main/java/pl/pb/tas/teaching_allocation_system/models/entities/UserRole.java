package pl.pb.tas.teaching_allocation_system.models.entities;

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
import pl.pb.tas.teaching_allocation_system.models.enums.RoleType;

/** Role of a user, optionally limited to a unit (scopeUnit = null means global). */
@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 30)
	private RoleType role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scope_unit_id")
	private OrganizationalUnit scopeUnit;

	public UserRole(AppUser user, RoleType role, OrganizationalUnit scopeUnit) {
		this.user = user;
		this.role = role;
		this.scopeUnit = scopeUnit;
	}
}
