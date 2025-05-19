package com.zaicev.task_tracker_backend.dto;

import com.zaicev.task_tracker_backend.models.TaskStatus;

public record TaskRequestDTO (Long id, String title, String description, TaskStatus status){
	
}
