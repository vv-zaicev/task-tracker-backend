package com.zaicev.task_tracker_backend.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zaicev.task_tracker_backend.dto.ErrorResponse;
import com.zaicev.task_tracker_backend.dto.TaskRequestDTO;
import com.zaicev.task_tracker_backend.dto.TaskResponseDTO;
import com.zaicev.task_tracker_backend.exceptions.UserNotFoundException;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.services.TaskService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		super();
		this.taskService = taskService;
	}

	@GetMapping
	public List<TaskResponseDTO> getTasks(@AuthenticationPrincipal User user) throws UserNotFoundException {
		return taskService.getUserTasks(user.getEmail());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TaskResponseDTO createTask(@AuthenticationPrincipal User user, @RequestBody TaskRequestDTO taskRequestDTO) throws UserNotFoundException {
		return taskService.createTask(taskRequestDTO, user.getEmail());
	}

	@PostMapping("/complete/{id}")
	public TaskResponseDTO completeTask(@AuthenticationPrincipal User user, @PathVariable Long id) throws UserNotFoundException {
		if (taskService.checkUserRights(id, user.getEmail())) {
			return taskService.completeTask(id);
		}
		throw new AccessDeniedException("User doesn't have permission to complete this task");
	}

	@PutMapping
	public TaskResponseDTO updateTask(@AuthenticationPrincipal User user, @RequestBody TaskRequestDTO taskRequestDTO) throws UserNotFoundException {
		if (taskService.checkUserRights(taskRequestDTO.id(), user.getEmail())) {
			return taskService.updateTask(taskRequestDTO);
		}

		throw new AccessDeniedException("User doesn't have permission to update this task");
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTask(@AuthenticationPrincipal User user, @PathVariable Long id) throws UserNotFoundException {
		if (taskService.checkUserRights(id, user.getEmail())) {
			taskService.deleteTask(id);
		} else {
			throw new AccessDeniedException("User doesn't have permission to delete this task");
		}
	}

	@ExceptionHandler({ UserNotFoundException.class, EntityNotFoundException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleUserNotFoundException(Exception ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
		return errorResponse;
	}

}
