package com.zaicev.task_tracker_backend.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.zaicev.task_tracker_backend.models.Task;
import com.zaicev.task_tracker_backend.models.User;


public interface TaskRepository extends CrudRepository<Task, Long>{
	List<Task> findByUser(User user);;
}
