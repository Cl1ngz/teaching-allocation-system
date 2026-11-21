package pl.pb.tas.teaching_allocation_system.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import pl.pb.tas.teaching_allocation_system.models.PageResponse;
import pl.pb.tas.teaching_allocation_system.models.TeacherListItem;
import pl.pb.tas.teaching_allocation_system.models.TeacherSummary;
import pl.pb.tas.teaching_allocation_system.repositories.TeacherRepository;

@Service
public class TeacherService {

	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;
	private static final Set<String> SORT_FIELDS = Set.of(
			"id", "firstName", "lastName", "email", "active",
			"degreeCode", "unitName", "positionName");
	private static final Map<String, String> SORT_PROPERTIES = Map.of(
			"id", "id",
			"firstName", "firstName",
			"lastName", "lastName",
			"email", "email",
			"active", "active",
			"degreeCode", "d.code",
			"unitName", "u.name",
			"positionName", "p.name");

	private final TeacherRepository teacherRepository;

	public TeacherService(TeacherRepository teacherRepository) {
		this.teacherRepository = teacherRepository;
	}

	public PageResponse<TeacherListItem> getTeachers(
			int page, Integer size, String[] sort) {
		if (page < 0) {
			throw new IllegalArgumentException("Parameter 'page' must be greater than or equal to 0.");
		}
		int resolvedSize = size == null ? DEFAULT_SIZE : size;
		if (resolvedSize < 1 || resolvedSize > MAX_SIZE) {
			throw new IllegalArgumentException("Parameter 'size' must be between 1 and 100.");
		}

		Sort resolvedSort = resolveSort(sort);
		Page<TeacherSummary> summaries = teacherRepository.findSummaries(
				LocalDate.now(), PageRequest.of(page, resolvedSize, resolvedSort));

		List<TeacherListItem> content = summaries.getContent().stream()
				.map(summary -> new TeacherListItem(
						summary.id(), summary.firstName(), summary.lastName(), summary.email(),
						summary.degreeCode(), summary.degreeName(),
						summary.unitId(), summary.unitName(), summary.positionName(),
						summary.active()))
				.toList();

		return new PageResponse<>(
				content,
				summaries.getNumber(),
				summaries.getSize(),
				summaries.getTotalElements(),
				summaries.getTotalPages());
	}

	private Sort resolveSort(String[] sort) {
		if (sort == null || sort.length == 0) {
			return Sort.by(
					Sort.Order.asc("lastName"),
					Sort.Order.asc("firstName"),
					Sort.Order.asc("id"));
		}

		Sort sortResult = Sort.unsorted();
		for (String value : sort) {
			if (value == null || value.isBlank()) {
				throw new IllegalArgumentException("Parameter 'sort' contains an invalid value.");
			}
			String[] parts = value.split(",", -1);
			String field = parts[0].trim();
			if (!SORT_FIELDS.contains(field)) {
				throw new IllegalArgumentException("Unsupported sort field: " + field + ".");
			}

			Sort.Direction direction = Sort.Direction.ASC;
			if (parts.length == 2) {
				try {
					direction = Sort.Direction.fromString(parts[1].trim().toUpperCase(Locale.ROOT));
				} catch (IllegalArgumentException exception) {
					throw new IllegalArgumentException(
							"Parameter 'sort' has an invalid direction. Use asc or desc.");
				}
			} else if (parts.length != 1) {
				throw new IllegalArgumentException("Parameter 'sort' must use the format field or field,direction.");
			}
			sortResult = sortResult.and(Sort.by(direction, SORT_PROPERTIES.get(field)));
		}
		return sortResult;
	}
}
