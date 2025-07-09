package com.zaicev.task_tracker_backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zaicev.task_tracker_backend.models.TaskStatus;

public record TaskResponseDTO(
		Long id,
		String title,
		String description,
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Moscow") LocalDateTime createdAt,
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Moscow") LocalDateTime completedAt,
		TaskStatus status) {
}
