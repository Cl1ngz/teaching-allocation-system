package pl.pb.tas.teaching_allocation_system.models;

import java.util.List;

/**
 * Paginated API response.
 */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {
}
