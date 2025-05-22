package com.zaicev.task_tracker_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.zaicev.task_tracker_backend.dto.TaskRequestDTO;
import com.zaicev.task_tracker_backend.dto.TaskResponseDTO;
import com.zaicev.task_tracker_backend.models.Task;
import com.zaicev.task_tracker_backend.models.TaskStatus;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.TaskRepository;
import com.zaicev.task_tracker_backend.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private TaskService taskService;

	private final String validEmail = "test@example.com";
	private final String invalidEmail = "invalid@example.com";
	private final Long validUserId = 1L;
	private final Long invalidUserId = 2L;
	private final String validTitle = "title";
	private final String validDescription = "description";
	private final Long validTaskId = 1L;
	private final Long invalidTaskId = 999L;

	private User testUser;
	private User invalidUser;
	private Task testTask;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setEmail(validEmail);
		testUser.setId(validUserId);

		invalidUser = new User();
		invalidUser.setEmail(invalidEmail);
		invalidUser.setId(invalidUserId);

		testTask = new Task();
		testTask.setId(validTaskId);
		testTask.setUser(testUser);
		testTask.setStatus(TaskStatus.IN_PROGRESS);
		testTask.setDescription(validDescription);
		testTask.setTitle(validTitle);
	}

	@Test
	void createTask_WithValidData_ReturnsTaskDTO() {
		// Arrange
		TaskRequestDTO requestDTO = new TaskRequestDTO(null, validTitle, validDescription, TaskStatus.IN_PROGRESS);
		when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(testUser));
		when(taskRepository.save(any(Task.class))).thenReturn(testTask);

		// Act
		TaskResponseDTO result = taskService.createTask(requestDTO, validEmail);

		// Assert
		assertNotNull(result);
		verify(taskRepository).save(any(Task.class));
		assertEquals(validTaskId, result.id());
	}

	@Test
	void updateTask_WithValidData_UpdatesTask() {
		TaskRequestDTO requestDTO = new TaskRequestDTO(validTaskId, validTitle, validDescription, TaskStatus.IN_PROGRESS);
		when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(testUser));
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));
		when(taskRepository.save(any(Task.class))).thenReturn(testTask);

		TaskResponseDTO result = taskService.updateTask(requestDTO, validEmail);

		assertNotNull(result);
		verify(taskRepository).save(any(Task.class));
		assertEquals(validTaskId, result.id());
		assertEquals(validTitle, result.title());
		assertEquals(validDescription, result.description());
	}
	
	@Test
	void updateTask_WithInvalidUser_ThrowsException() {
		TaskRequestDTO requestDTO = new TaskRequestDTO(validTaskId, validTitle, validDescription, TaskStatus.IN_PROGRESS);
		when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.of(invalidUser));
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));

		assertThrows(AccessDeniedException.class, () -> taskService.updateTask(requestDTO, invalidEmail));
	}

	@Test
	void deleteTask_WithValidIdAndUser_DeletesTask() {
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));
		when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(testUser));
		
		taskService.deleteTask(validTaskId, validEmail);

		verify(taskRepository).deleteById(validTaskId);
	}
	
	@Test
	void deleteTask_WithInvalidId_ThrowsException() {
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.empty());
		
		assertThrows(EntityNotFoundException.class, () -> taskService.deleteTask(validTaskId, validEmail));
	}
	
	@Test
	void deleteTask_WithInvalidUser_ThrowsException() {
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));
		when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.of(invalidUser));
		
		assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(validTaskId, invalidEmail));
	}

	@Test
	void getUserTasks_WithValidUser_ReturnsTaskList() {
		List<Task> tasks = Collections.singletonList(testTask);
		when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(testUser));
		when(taskRepository.findByUser(testUser)).thenReturn(tasks);

		List<TaskResponseDTO> result = taskService.getUserTasks(validEmail);

		assertEquals(1, result.size());
		assertEquals(validTaskId, result.get(0).id());
		assertEquals(validTitle, result.get(0).title());
		assertEquals(validDescription, result.get(0).description());
	}

	@Test
	void completeTask_WithValidIdAndUser_UpdatesStatus() {
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));
		when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(testUser));

		TaskResponseDTO result = taskService.completeTask(validTaskId, validEmail);

		assertEquals(TaskStatus.COMPLETE, testTask.getStatus());
		verify(taskRepository).save(testTask);
		assertEquals(TaskStatus.COMPLETE, result.status());
	}

	@Test
	void completeTask_WithInvalidId_ThrowsException() {
		when(taskRepository.findById(invalidTaskId)).thenReturn(Optional.empty());
		
		assertThrows(EntityNotFoundException.class,
				() -> taskService.completeTask(invalidTaskId, validEmail));
	}
	
	@Test
	void competeTask_WithInvalidUser_ThrowsException() {
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));
		when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.of(invalidUser));
		
		assertThrows(AccessDeniedException.class, () -> taskService.completeTask(validTaskId, invalidEmail));
	}

}
