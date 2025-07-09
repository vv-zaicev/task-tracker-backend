package com.zaicev.task_tracker_backend.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.zaicev.task_tracker_backend.converters.TaskDTOConverter;
import com.zaicev.task_tracker_backend.dto.TaskRequestDTO;
import com.zaicev.task_tracker_backend.dto.TaskResponseDTO;
import com.zaicev.task_tracker_backend.exceptions.UserNotFoundException;
import com.zaicev.task_tracker_backend.models.Task;
import com.zaicev.task_tracker_backend.models.TaskStatus;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.TaskRepository;
import com.zaicev.task_tracker_backend.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;

@Service
public class TaskService {
	private final TaskRepository taskRepository;

	private final UserRepository userRepository;

	@Setter
	private TaskDTOConverter taskDTOConverter = new TaskDTOConverter() {};

	public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;

	}

	public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO, String userEmail) throws UserNotFoundException{
		Task task = taskDTOConverter.toEntity(taskRequestDTO);

		task.setCreatedAt(LocalDateTime.now());
		task.setUser(userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail)));
		task = taskRepository.save(task);

		return taskDTOConverter.toDTO(task);
	}

	public TaskResponseDTO updateTask(TaskRequestDTO taskRequestDTO){
		Task task = taskRepository.findById(taskRequestDTO.id()).orElseThrow(() -> new EntityNotFoundException("task with %d id not found".formatted(taskRequestDTO.id())));

		task.setTitle(taskRequestDTO.title());
		task.setDescription(taskRequestDTO.description());
		task.setStatus(taskRequestDTO.status());
		
		taskRepository.save(task);

		return taskDTOConverter.toDTO(task);
	}

	public void deleteTask(Long taskId) {
		taskRepository.deleteById(taskId);
	}

	public List<TaskResponseDTO> getUserTasks(String userEmail) throws UserNotFoundException{
		User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));
		List<Task> tasks = taskRepository.findByUser(user);

		return tasks.stream().map(taskDTOConverter::toDTO).toList();
	}

	public TaskResponseDTO completeTask(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new EntityNotFoundException(String.format("Entity with %d id not found", taskId)));
		task.setStatus(TaskStatus.COMPLETE);
		task.setComletedAt(LocalDateTime.now());
		taskRepository.save(task);

		return taskDTOConverter.toDTO(task);
	}

	public boolean checkUserRights(Long taskId, String userEmail) throws UserNotFoundException{
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new EntityNotFoundException(String.format("Entity with %d id not found", taskId)));
		User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));

		return task.getUser() == user;
	}

}
