package pl.pb.tas.teaching_allocation_system.models;

/**
 * One row of the teacher list: the teacher, the academic degree and the primary
 * employment in force on the requested day. The unit and position fields are
 * null when the teacher has no such employment.
 */
public record TeacherSummary(
		Long id,
		String firstName,
		String lastName,
		String email,
		boolean active,
		String degreeCode,
		String degreeName,
		Long unitId,
		String unitName,
		String positionName) {
}
