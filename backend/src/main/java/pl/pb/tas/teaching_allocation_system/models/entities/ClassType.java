package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Class type (lecture, lab, ...) with the weight converting hours into pensum hours. */
@Entity
@Table(name = "class_types")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassType extends BaseEntity {

	@Column(name = "code", nullable = false, unique = true, length = 30)
	private String code;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "hour_weight", nullable = false, precision = 4, scale = 2)
	private BigDecimal hourWeight = BigDecimal.ONE;

	public ClassType(String code, String name, BigDecimal hourWeight) {
		this.code = code;
		this.name = name;
		this.hourWeight = hourWeight;
	}
}
