package pl.pb.tas.teaching_allocation_system.models.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pb.tas.teaching_allocation_system.models.enums.UnitType;

/**
 * Unit in the hierarchy faculty -> department -> division (self-reference).
 * A faculty has no parent. Allowed parent types and the absence of cycles are
 * validated in the service layer.
 */
@Entity
@Table(name = "organizational_units")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationalUnit extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private OrganizationalUnit parent;

	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "parent")
	@OrderBy("name ASC")
	private List<OrganizationalUnit> children = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "unit_type", nullable = false, length = 20)
	private UnitType unitType;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "short_name", length = 50)
	private String shortName;

	@Column(name = "usos_id", unique = true, length = 50)
	private String usosId;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	public OrganizationalUnit(UnitType unitType, String name, OrganizationalUnit parent) {
		this.unitType = unitType;
		this.name = name;
		this.parent = parent;
	}
}
