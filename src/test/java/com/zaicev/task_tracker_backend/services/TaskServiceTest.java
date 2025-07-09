package com.zaicev.task_tracker_backend.services;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zaicev.task_tracker_backend.dto.TaskRequestDTO;
import com.zaicev.task_tracker_backend.dto.TaskResponseDTO;
import com.zaicev.task_tracker_backend.exceptions.UserNotFoundException;
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
	void createTask_WithValidData_ReturnsTaskDTO() throws Exception{
		TaskRequestDTO requestDTO = new TaskRequestDTO(null, validTitle, validDescription, TaskStatus.IN_PROGRESS);
		when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(testUser));
		when(taskRepository.save(any(Task.class))).thenReturn(testTask);

		TaskResponseDTO result = taskService.createTask(requestDTO, validEmail);

		assertNotNull(result);
		verify(taskRepository).save(any(Task.class));
		assertEquals(validTaskId, result.id());
		assertEquals(validTitle, result.title());
		assertEquals(validDescription, result.description());
		assertEquals(TaskStatus.IN_PROGRESS, result.status());
		assertNull(result.completedAt());
	}
	
	@Test
	void createTask_WithoutUser_ThrowsUserNotFoundException(){
		when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());
		TaskRequestDTO requestDTO = new TaskRequestDTO(null, validTitle, validDescription, TaskStatus.IN_PROGRESS);
		
		assertThrows(UserNotFoundException.class, () -> taskService.createTask(requestDTO, invalidEmail));
	}

	@Test
	void updateTask_WithValidData_UpdatesTask() throws Exception{
		TaskRequestDTO requestDTO = new TaskRequestDTO(validTaskId, validTitle, validDescription, TaskStatus.IN_PROGRESS);
		LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
		LocalDateTime completeAt = LocalDateTime.of(2023, 1, 1, 23, 0);
		Task updatedTask = new Task(validTaskId, "somTitle", "somDesc", createdAt, completeAt, TaskStatus.COMPLETE, testUser);
		
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(updatedTask));

		TaskResponseDTO result = taskService.updateTask(requestDTO);

		assertNotNull(result);
		verify(taskRepository).save(any(Task.class));
		assertEquals(validTaskId, result.id());
		assertEquals(validTitle, result.title());
		assertEquals(validDescription, result.description());
		assertEquals(TaskStatus.IN_PROGRESS, result.status());
		assertEquals(createdAt, result.createdAt());
		assertEquals(completeAt, result.completedAt());
	}
	
	@Test
	void updateTask_WithoutUser_ThrowsEntityNotFoundException(){
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.empty());
		TaskRequestDTO requestDTO = new TaskRequestDTO(validTaskId, validTitle, validDescription, TaskStatus.IN_PROGRESS);
		
		assertThrows(EntityNotFoundException.class, () -> taskService.updateTask(requestDTO));
	}

	@Test
	void deleteTask_WithValidId_DeletesTask() {

		taskService.deleteTask(validTaskId);

		verify(taskRepository).deleteById(validTaskId);
	}

	@Test
	void getUserTasks_WithValidUser_ReturnsTaskList() throws Exception{
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
	void getUserTasks_WithoutUser_ThrowsUserNotFoundException(){
		when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());
		
		assertThrows(UserNotFoundException.class, () -> taskService.getUserTasks(invalidEmail));
	}

	@Test
	void completeTask_WithValidId_UpdatesStatus() {
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));

		LocalDateTime before = LocalDateTime.now();
		TaskResponseDTO result = taskService.completeTask(validTaskId);
		LocalDateTime after = LocalDateTime.now();
		
		assertEquals(TaskStatus.COMPLETE, testTask.getStatus());
		verify(taskRepository).save(testTask);
		assertEquals(TaskStatus.COMPLETE, result.status());
		assertNotNull(result.completedAt());
		assertTrue(!result.completedAt().isBefore(before) && !result.completedAt().isAfter(after));
	}

	@Test
	void completeTask_WithInvalidId_ThrowsException() {
		when(taskRepository.findById(invalidTaskId)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> taskService.completeTask(invalidTaskId));
	}

	@Test
	void checkUserRights_WithValidUserAndId_ReturnsTrue() throws Exception{
		when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(testUser));
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));

		boolean result = taskService.checkUserRights(validTaskId, validEmail);

		assertEquals(true, result);
	}

	@Test
	void checkUserRights_WithInvalidUser_ReturnsFalse() throws Exception{
		when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.of(invalidUser));
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));

		boolean result = taskService.checkUserRights(validTaskId, invalidEmail);

		assertEquals(false, result);
	}
	
	@Test
	void checkUserRights_WithoutUser_ThrowsUserNotFoundException(){
		when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());
		when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(testTask));
		
		assertThrows(UserNotFoundException.class, () -> taskService.checkUserRights(validTaskId, invalidEmail));
	}

	@Test
	void checkUserRights_WithInvalidId_ThrowsException() {
		when(taskRepository.findById(invalidTaskId)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> taskService.checkUserRights(invalidTaskId, validEmail));
	}
}
