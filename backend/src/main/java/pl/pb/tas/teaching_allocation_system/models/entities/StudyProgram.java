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
import pl.pb.tas.teaching_allocation_system.models.enums.StudyLevel;

/** Study program run by a faculty. */
@Entity
@Table(name = "study_programs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyProgram extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unit_id", nullable = false)
	private OrganizationalUnit unit;

	@Column(name = "code", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "name", nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false, length = 20)
	private StudyLevel level;

	@Column(name = "usos_id", unique = true, length = 50)
	private String usosId;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	public StudyProgram(OrganizationalUnit unit, String code, String name, StudyLevel level) {
		this.unit = unit;
		this.code = code;
		this.name = name;
		this.level = level;
	}
}
