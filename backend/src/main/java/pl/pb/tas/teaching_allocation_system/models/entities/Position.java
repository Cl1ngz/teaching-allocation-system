package pl.pb.tas.teaching_allocation_system.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dictionary of positions. The staff group is not a property of the position
 * but of the employment (see Employment.employeeGroup).
 */
@Entity
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position extends BaseEntity {

	@Column(name = "code", nullable = false, unique = true, length = 30)
	private String code;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	public Position(String code, String name) {
		this.code = code;
		this.name = name;
	}
}
