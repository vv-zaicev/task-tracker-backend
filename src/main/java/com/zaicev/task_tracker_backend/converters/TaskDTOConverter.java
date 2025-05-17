package com.zaicev.task_tracker_backend.converters;

import com.zaicev.task_tracker_backend.dto.TaskRequestDTO;
import com.zaicev.task_tracker_backend.dto.TaskResponseDTO;
import com.zaicev.task_tracker_backend.models.Task;

public interface TaskDTOConverter {
	
	default public Task toEntity(TaskRequestDTO taskRequestDTO) {
		Task task = new Task();
		task.setTitle(taskRequestDTO.title());
		task.setDescription(taskRequestDTO.description());
		task.setStatus(taskRequestDTO.status());
		return task;
	};
	
	default public TaskResponseDTO toDTO(Task task) {
		return new TaskResponseDTO(task.getId(), task.getTitle(), task.getDescription(), task.getStatus());
	};
}
