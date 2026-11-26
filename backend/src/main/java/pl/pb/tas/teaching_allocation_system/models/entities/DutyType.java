package pl.pb.tas.teaching_allocation_system.models.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pb.tas.teaching_allocation_system.models.enums.DutyCategory;

/** Dictionary of additional duties that reduce the pensum. */
@Entity
@Table(name = "duty_types")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DutyType extends BaseEntity {

	@Column(name = "code", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false, length = 30)
	private DutyCategory category;

	/** Default yearly reduction; null = set by hand for each duty. */
	@Column(name = "default_reduction_hours", precision = 6, scale = 2)
	private BigDecimal defaultReductionHours;

	public DutyType(String code, String name, DutyCategory category, BigDecimal defaultReductionHours) {
		this.code = code;
		this.name = name;
		this.category = category;
		this.defaultReductionHours = defaultReductionHours;
	}
}
