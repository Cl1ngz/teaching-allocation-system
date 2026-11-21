package pl.pb.tas.teaching_allocation_system.models;

/**
 * Public API representation of one teacher on the paginated list.
 */
public record TeacherListItem(
		Long id,
		String firstName,
		String lastName,
		String email,
		String degreeCode,
		String degreeName,
		Long unitId,
		String unitName,
		String positionName,
		boolean active) {
}
