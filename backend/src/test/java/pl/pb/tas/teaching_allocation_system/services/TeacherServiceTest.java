package pl.pb.tas.teaching_allocation_system.services;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pl.pb.tas.teaching_allocation_system.models.PageResponse;
import pl.pb.tas.teaching_allocation_system.models.TeacherListItem;
import pl.pb.tas.teaching_allocation_system.models.TeacherSummary;
import pl.pb.tas.teaching_allocation_system.repositories.TeacherRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeacherServiceTest {

	private final TeacherRepository repository = mock(TeacherRepository.class);
	private final TeacherService service = new TeacherService(repository);

	@Test
	void mapsSummaryAndPaginates() {
		TeacherSummary summary = new TeacherSummary(
				1L, "Jan", "Kowalski", "jan@example.com", true,
				"PROF", "profesor", 10L, "Wydzial", "profesor");
		when(repository.findSummaries(any(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(summary)));

		PageResponse<TeacherListItem> result = service.getTeachers(0, 20, null);

		assertThat(result.content()).hasSize(1);
		assertThat(result.content().getFirst().unitName()).isEqualTo("Wydzial");
		assertThat(result.totalElements()).isEqualTo(1);
	}

	@Test
	void rejectsInvalidPageSizeAndSort() {
		assertThatThrownBy(() -> service.getTeachers(-1, 20, null))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.getTeachers(0, 101, null))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.getTeachers(0, 20, new String[]{"unknown,asc"}))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void returnsTheRequestedPageAndTotals() {
		TeacherSummary first = new TeacherSummary(
				1L, "Adam", "Abacki", "adam@example.com", true,
				"DR", "doktor", null, null, null);
		PageImpl<TeacherSummary> page = new PageImpl<>(
				List.of(first), PageRequest.of(1, 1), 3);
		when(repository.findSummaries(any(), any(Pageable.class))).thenReturn(page);

		PageResponse<TeacherListItem> result = service.getTeachers(1, 1, null);

		assertThat(result.page()).isEqualTo(1);
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.totalElements()).isEqualTo(3);
		assertThat(result.totalPages()).isEqualTo(3);
		assertThat(result.content()).hasSize(1);
	}
}
