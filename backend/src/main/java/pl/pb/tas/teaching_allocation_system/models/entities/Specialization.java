package pl.pb.tas.teaching_allocation_system.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dictionary of teaching specializations and competences. */
@Entity
@Table(name = "specializations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Specialization extends BaseEntity {

	@Column(name = "code", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "description")
	private String description;

	public Specialization(String code, String name) {
		this.code = code;
		this.name = name;
	}
}
