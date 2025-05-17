package com.zaicev.task_tracker_backend.dto;

import com.zaicev.task_tracker_backend.models.TaskStatus;

public record TaskResponseDTO(Long id, String title, String description, TaskStatus status) {

}
