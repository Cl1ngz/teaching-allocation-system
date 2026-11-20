package pl.pb.tas.teaching_allocation_system.models.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Academic teacher. Membership in units, position and staff group are stored
 * in employments, not here (decision D2 of the data model).
 */
@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academic_degree_id", nullable = false)
	private AcademicDegree academicDegree;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "usos_id", unique = true, length = 50)
	private String usosId;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "teacher")
	private List<Employment> employments = new ArrayList<>();

	public Teacher(AcademicDegree academicDegree, String firstName, String lastName, String email) {
		this.academicDegree = academicDegree;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}
}
