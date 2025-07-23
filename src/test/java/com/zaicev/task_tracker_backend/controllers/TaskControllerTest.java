package com.zaicev.task_tracker_backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaicev.task_tracker_backend.config.TestSecurityConfig;
import com.zaicev.task_tracker_backend.dto.TaskRequestDTO;
import com.zaicev.task_tracker_backend.dto.TaskResponseDTO;
import com.zaicev.task_tracker_backend.exceptions.UserNotFoundException;
import com.zaicev.task_tracker_backend.models.TaskStatus;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.services.TaskService;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
public class TaskControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TaskService taskService;

	private User testUser;

	private Authentication authentication;
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

	private LocalDateTime now = LocalDateTime.now();

	private TaskResponseDTO responseDTO = new TaskResponseDTO(1L, "title", "description", now, now, TaskStatus.IN_PROGRESS);

	private TaskRequestDTO requestDTO = new TaskRequestDTO(1L, "somTitle", "desc", TaskStatus.IN_PROGRESS);

	@BeforeEach
	void initialize() {
		testUser = User.builder().email("test@example.com").username("testUser").build();
		authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
	}

	@Test
	void getTasks_WithAuthentication_ReturnsTaskList() throws Exception {
		when(taskService.getUserTasks(testUser.getEmail())).thenReturn(List.of(responseDTO));

		mockMvc.perform(get("/tasks").with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].title").value("title"))
				.andExpect(jsonPath("$[0].description").value("description"))
				.andExpect(jsonPath("$[0].completedAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$[0].createdAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$[0].status").value(TaskStatus.IN_PROGRESS.toString()));
	}

	@Test
	void getTasks_ThrowUserNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new UserNotFoundException(testUser.getEmail());
		when(taskService.getUserTasks(testUser.getEmail())).thenThrow(exception);

		mockMvc.perform(get("/tasks").with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());

	}

	@Test
	void getTasks_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/tasks"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void completeTask_WithCorrectData_ShouldReturnTask() throws Exception {
		when(taskService.checkUserRights(1L, "test@example.com")).thenReturn(true);
		when(taskService.completeTask(1L)).thenReturn(responseDTO);

		mockMvc.perform(post("/tasks/complete/1")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.title").value("title"))
				.andExpect(jsonPath("$.description").value("description"))
				.andExpect(jsonPath("$.createdAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.toString()));
	}
	
	@Test
	void complete_ThrowUserNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new UserNotFoundException(testUser.getEmail());
		when(taskService.checkUserRights(1L, "test@example.com")).thenThrow(exception);

		mockMvc.perform(post("/tasks/complete/1")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());

	}

	@Test
	void complete_ThrowEntityNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new EntityNotFoundException("Entity not found");
		when(taskService.checkUserRights(1L, "test@example.com")).thenReturn(true);
		when(taskService.completeTask(1L)).thenThrow(exception);

		mockMvc.perform(post("/tasks/complete/1")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());

	}

	@Test
	void completeTask_WithoutCsrf_ShouldReturnForbidden() throws Exception {
		mockMvc.perform(post("/tasks/complete/1"))
				.andExpect(status().isForbidden());
	}

	@Test
	void completeTask_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(post("/tasks/complete/1").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getCompletedTasks_WithCorrectData_shouldReturnListOfTasks() throws Exception {
		Long userId = 1L;
		int top = 3;
		LocalDateTime from = LocalDateTime.of(2023, 1, 1, 0, 0);
		List<TaskResponseDTO> tasks = List.of(responseDTO);

		when(taskService.getTopCompletedUserTaskFromDate(eq(userId), eq(top), eq(from)))
				.thenReturn(tasks);

		mockMvc.perform(get("/tasks/completed")
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
				.param("userId", userId.toString())
				.param("top", String.valueOf(top))
				.param("from", from.toString()))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].title").value("title"))
				.andExpect(jsonPath("$[0].description").value("description"))
				.andExpect(jsonPath("$[0].completedAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$[0].createdAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$[0].status").value(TaskStatus.IN_PROGRESS.toString()));
	}
	
	@Test
	void getCompletedTasks_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/tasks/completed"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getInProgressTasks_WithCorrectData_shouldReturnListOfTasks() throws Exception {
		Long userId = 2L;
		int top = 2;
		List<TaskResponseDTO> tasks = List.of(responseDTO);

		when(taskService.getTopInProgressUserTask(eq(userId), eq(top)))
				.thenReturn(tasks);

		mockMvc.perform(get("/tasks/inProgress")
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
				.param("userId", userId.toString())
				.param("top", String.valueOf(top)))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].title").value("title"))
				.andExpect(jsonPath("$[0].description").value("description"))
				.andExpect(jsonPath("$[0].completedAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$[0].createdAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$[0].status").value(TaskStatus.IN_PROGRESS.toString()));
	}
	
	@Test
	void getInProgressTasks_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/completed"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createTask_WithCorrectData_ShouldReturnCreatedTask() throws Exception {
		when(taskService.createTask(any(), eq(testUser.getEmail()))).thenReturn(responseDTO);

		mockMvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.title").value("title"))
				.andExpect(jsonPath("$.description").value("description"))
				.andExpect(jsonPath("$.createdAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.toString()));
	}

	@Test
	void createTask_ThrowUserNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new UserNotFoundException(testUser.getEmail());
		when(taskService.createTask(any(), eq(testUser.getEmail()))).thenThrow(exception);

		mockMvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());

	}

	@Test
	void createTask_ThrowEntityNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new EntityNotFoundException("Entity not found");
		when(taskService.createTask(any(), eq(testUser.getEmail()))).thenThrow(exception);

		mockMvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void createTask_WithoutCsrf_ShouldReturnForbidden() throws Exception {
		mockMvc.perform(post("/tasks"))
				.andExpect(status().isForbidden());
	}

	@Test
	void createTask_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(post("/tasks").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void updateTask_WithCorrectData_ShouldReturnUpdatedTask() throws Exception {
		when(taskService.checkUserRights(1L, "test@example.com")).thenReturn(true);
		when(taskService.updateTask(any())).thenReturn(responseDTO);

		mockMvc.perform(put("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.title").value("title"))
				.andExpect(jsonPath("$.description").value("description"))
				.andExpect(jsonPath("$.createdAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.toString()));
	}

	@Test
	void updateTask_ThrowUserNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new UserNotFoundException(testUser.getEmail());
		when(taskService.checkUserRights(1L, "test@example.com")).thenThrow(exception);

		mockMvc.perform(put("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());

	}

	@Test
	void updateTask_ThrowEntityNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new EntityNotFoundException("Entity not found");
		when(taskService.checkUserRights(1L, "test@example.com")).thenReturn(true);
		when(taskService.updateTask(any())).thenThrow(exception);

		mockMvc.perform(put("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());

	}

	@Test
	void updateTask_WithoutUserRights_ShouldReturnForbidden() throws Exception {
		when(taskService.checkUserRights(1L, "test@example.com")).thenReturn(false);

		mockMvc.perform(put("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isForbidden());
	}

	@Test
	void updateTask_WithoutCsrf_ShouldReturnForbidden() throws Exception {
		mockMvc.perform(put("/tasks"))
				.andExpect(status().isForbidden());
	}

	@Test
	void updateTask_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(put("/tasks").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deleteTask_WithCorrectData_ShouldReturnNoContent() throws Exception {
		when(taskService.checkUserRights(1L, "test@example.com")).thenReturn(true);

		mockMvc.perform(delete("/tasks/1")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNoContent());
		verify(taskService).deleteTask(1L);
	}

	@Test
	void deleteTask_ThrowUserNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new UserNotFoundException(testUser.getEmail());
		when(taskService.checkUserRights(1L, "test@example.com")).thenThrow(exception);

		mockMvc.perform(delete("/tasks/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());

	}

	@Test
	void deleteTask_ThrowEntityNotFoundException_ShouldReturnNotFount() throws Exception {
		Exception exception = new EntityNotFoundException("Entity not found");
		when(taskService.checkUserRights(1L, "test@example.com")).thenReturn(true);
		doThrow(exception).when(taskService).deleteTask(1L);

		mockMvc.perform(delete("/tasks/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(exception.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());

	}

	@Test
	void deleteTask_WithoutUserRights_ShouldReturnForbidden() throws Exception {
		when(taskService.checkUserRights(1L, "test@example.com")).thenReturn(false);

		mockMvc.perform(delete("/tasks/1")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isForbidden());
	}

	@Test
	void deleteTask_WithoutCsrf_ShouldReturnForbidden() throws Exception {
		mockMvc.perform(delete("/tasks/1"))
				.andExpect(status().isForbidden());
	}

	@Test
	void deleteTask_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(delete("/tasks/1").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isUnauthorized());
	}
}
