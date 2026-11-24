package pl.pb.tas.teaching_allocation_system.models.entities;

import java.time.Instant;

import org.hibernate.annotations.ColumnTransformer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Append-only change log (deviation O3 of the data model): no technical
 * columns, entityId without a foreign key, values as jsonb.
 *
 * The jsonb columns are mapped as JSON text with a cast on write instead of
 * Hibernate's JSON type: that type needs a JSON FormatMapper, and Hibernate's
 * automatic one does not cover Jackson 3 (Spring Boot 4) everywhere yet. The
 * application passes ready JSON strings. Whether this table or Hibernate Envers
 * is used for auditing is still open (Q10).
 */
@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "entity_type", nullable = false, updatable = false, length = 100)
	private String entityType;

	@Column(name = "entity_id", nullable = false, updatable = false)
	private Long entityId;

	@Column(name = "action", nullable = false, updatable = false, length = 20)
	private String action;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by", updatable = false)
	private AppUser changedBy;

	@Column(name = "changed_at", nullable = false, updatable = false)
	private Instant changedAt;

	@ColumnTransformer(write = "?::jsonb")
	@Column(name = "old_values", columnDefinition = "jsonb", updatable = false)
	private String oldValues;

	@ColumnTransformer(write = "?::jsonb")
	@Column(name = "new_values", columnDefinition = "jsonb", updatable = false)
	private String newValues;

	public AuditLog(String entityType, Long entityId, String action, AppUser changedBy,
			String oldValues, String newValues) {
		this.entityType = entityType;
		this.entityId = entityId;
		this.action = action;
		this.changedBy = changedBy;
		this.changedAt = Instant.now();
		this.oldValues = oldValues;
		this.newValues = newValues;
	}
}
