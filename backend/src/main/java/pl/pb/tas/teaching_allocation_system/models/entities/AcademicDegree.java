package pl.pb.tas.teaching_allocation_system.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dictionary of academic degrees and titles; compared by rankOrder. */
@Entity
@Table(name = "academic_degrees")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicDegree extends BaseEntity {

	@Column(name = "code", nullable = false, unique = true, length = 30)
	private String code;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "rank_order", nullable = false, unique = true)
	private Integer rankOrder;

	public AcademicDegree(String code, String name, Integer rankOrder) {
		this.code = code;
		this.name = name;
		this.rankOrder = rankOrder;
	}
}
