package com.zaicev.task_tracker_backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.zaicev.task_tracker_backend.converters.DefaultTaskDTOConverter;
import com.zaicev.task_tracker_backend.converters.TaskDTOConverter;
import com.zaicev.task_tracker_backend.dto.TaskRequestDTO;
import com.zaicev.task_tracker_backend.dto.TaskResponseDTO;
import com.zaicev.task_tracker_backend.models.Task;
import com.zaicev.task_tracker_backend.models.TaskStatus;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.TaskRepository;
import com.zaicev.task_tracker_backend.repository.UserRepository;
import com.zaicev.task_tracker_backend.security.exceptions.UserNotFoundException;

import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;

@Service
public class TaskService {
	private final TaskRepository taskRepository;
	
	private final UserRepository userRepository;
	
	@Setter
	private TaskDTOConverter taskDTOConverter = new DefaultTaskDTOConverter();

	public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
		
	}
	 
	public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO, String userEmail) {
		Task task = taskDTOConverter.toEntity(taskRequestDTO);
		task.setUser(userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail)));
		task = taskRepository.save(task);
		return taskDTOConverter.toDTO(task);
	}
	
	public TaskResponseDTO updateTask(TaskRequestDTO taskRequestDTO, String userEmail) {
		Task task = taskDTOConverter.toEntity(taskRequestDTO);
		task.setUser(userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail)));
		task = taskRepository.save(task);
		return taskDTOConverter.toDTO(task);
	}
	
	public void deleteTask(Long id) {
		taskRepository.deleteById(id);
	}
	
	public List<TaskResponseDTO> getUserTasks(String userEmail){
		User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));
		List<Task> tasks = taskRepository.findByUser(user);
		return tasks.stream().map(taskDTOConverter::toDTO).toList();
	}
	
	public TaskResponseDTO completeTask(Long id) {
		Task task = taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("Entity with %d id not found", id)));
		task.setStatus(TaskStatus.COMPLETE);
		taskRepository.save(task);
		return taskDTOConverter.toDTO(task);
	}
}
