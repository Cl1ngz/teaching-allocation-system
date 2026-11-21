package pl.pb.tas.teaching_allocation_system.controllers;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.pb.tas.teaching_allocation_system.exceptions.GlobalExceptionHandler;
import pl.pb.tas.teaching_allocation_system.models.PageResponse;
import pl.pb.tas.teaching_allocation_system.models.TeacherListItem;
import pl.pb.tas.teaching_allocation_system.security.SecurityConfig;
import pl.pb.tas.teaching_allocation_system.services.TeacherService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TeacherControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TeacherService teacherService;

	@Test
	void returnsEmptyPage() throws Exception {
		when(teacherService.getTeachers(0, null, null))
				.thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

		mockMvc.perform(get("/api/v1/teachers").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.totalPages").value(0));
	}

	@Test
	void returnsTeacherWithoutEmployment() throws Exception {
		TeacherListItem teacher = new TeacherListItem(
				7L, "Anna", "Nowak", "anna@example.com",
				"DR", "doktor", null, null, null, true);
		when(teacherService.getTeachers(0, null, null))
				.thenReturn(new PageResponse<>(List.of(teacher), 0, 20, 1, 1));

		mockMvc.perform(get("/api/v1/teachers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(7))
				.andExpect(jsonPath("$.content[0].unitName").value((Object) null))
				.andExpect(jsonPath("$.content[0].positionName").value((Object) null))
				.andExpect(jsonPath("$.content[0].active").value(true));
	}

	@Test
	void rejectsInvalidSize() throws Exception {
		mockMvc.perform(get("/api/v1/teachers").param("size", "101"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void mapsDatabaseFailureToServiceUnavailable() throws Exception {
		when(teacherService.getTeachers(0, null, null))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException("down"));

		mockMvc.perform(get("/api/v1/teachers"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.code").value("SERVICE_UNAVAILABLE"))
				.andExpect(jsonPath("$.status").value(503))
				.andExpect(jsonPath("$.detail").value("The service is temporarily unavailable. Try again later."));
	}
}
