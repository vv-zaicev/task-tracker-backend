package com.zaicev.task_tracker_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.zaicev.task_tracker_backend.models.Task;
import com.zaicev.task_tracker_backend.models.User;

public interface TaskRepository extends CrudRepository<Task, Long> {
	List<Task> findByUser(User user);

	@Query(value = "SELECT * FROM tasks WHERE owner_id = :userId AND completed_at >= :from AND status = 'COMPLETE' ORDER BY completed_at LIMIT :top", nativeQuery = true)
	List<Task> findTopCompletedUserTaskFromDate(@Param("userId") Long userId, @Param("from") LocalDateTime fromLocalDateTime, @Param("top") int top);

	@Query(value = "SELECT * FROM tasks WHERE owner_id = :userId AND status = 'IN_PROGRESS' ORDER BY created_at LIMIT :top", nativeQuery = true)
	List<Task> findTopInProgressUserTask(@Param("userId") Long userId, @Param("top") int top);
}
