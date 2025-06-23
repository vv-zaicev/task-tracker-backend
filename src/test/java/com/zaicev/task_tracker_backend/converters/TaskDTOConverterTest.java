package com.zaicev.task_tracker_backend.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zaicev.task_tracker_backend.dto.TaskRequestDTO;
import com.zaicev.task_tracker_backend.dto.TaskResponseDTO;
import com.zaicev.task_tracker_backend.models.Task;
import com.zaicev.task_tracker_backend.models.TaskStatus;

@ExtendWith(MockitoExtension.class)
public class TaskDTOConverterTest {
	private final TaskDTOConverter taskDTOConverter = new TaskDTOConverter() {
	};

	@Test
	void toEntity_shouldMapAllFieldsCorrectly() {
		TaskRequestDTO requestDTO = new TaskRequestDTO(
				1L,
				"Test Title",
				"Test Description",
				TaskStatus.IN_PROGRESS);

		Task result = taskDTOConverter.toEntity(requestDTO);

		assertEquals(1L, result.getId());
		assertEquals("Test Title", result.getTitle());
		assertEquals("Test Description", result.getDescription());
		assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
	}

	@Test
	void toDTO_shouldMapAllFieldsIncludingCreatedAt() {
		Task taskEntity = new Task();
		LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		taskEntity.setId(2L);
		taskEntity.setTitle("DTO Test");
		taskEntity.setDescription("DTO Description");
		taskEntity.setStatus(TaskStatus.COMPLETE);
		taskEntity.setCreatedAt(dateTime);

		TaskResponseDTO result = taskDTOConverter.toDTO(taskEntity);

		assertEquals(2L, result.id());
		assertEquals("DTO Test", result.title());
		assertEquals("DTO Description", result.description());
		assertEquals(TaskStatus.COMPLETE, result.status());
		assertEquals(dateTime, result.createdAt());
	}
}
