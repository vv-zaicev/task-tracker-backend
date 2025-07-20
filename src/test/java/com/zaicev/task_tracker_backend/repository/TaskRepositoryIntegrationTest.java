package com.zaicev.task_tracker_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import com.zaicev.task_tracker_backend.models.Task;
import com.zaicev.task_tracker_backend.models.TaskStatus;
import com.zaicev.task_tracker_backend.models.User;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskRepositoryIntegrationTest {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TestEntityManager entityManager;

	private User testUser;
	private User anotherUser;

	@BeforeEach
	void setUp() {
		testUser = entityManager.persistFlushFind(User.builder()
				.email("user@example.com")
				.username("user1")
				.password("password")
				.build());

		anotherUser = entityManager.persistFlushFind(User.builder()
				.email("another@example.com")
				.username("user2")
				.password("somPass")
				.build());

		createTask("first", testUser, TaskStatus.COMPLETE, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(2));
		createTask("second", testUser, TaskStatus.COMPLETE, LocalDateTime.now(), LocalDateTime.now().minusDays(1));
		createTask("third", testUser, TaskStatus.COMPLETE, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(12));

		createTask("fourth", testUser, TaskStatus.IN_PROGRESS, null, LocalDateTime.now().minusHours(1));
		createTask("fifth", testUser, TaskStatus.IN_PROGRESS, null, LocalDateTime.now().minusHours(2));
	}

	private Task createTask(String title, User user, TaskStatus status, LocalDateTime completedAt, LocalDateTime createdAt) {
		Task task = new Task();
		task.setTitle(title);
		task.setDescription("Test Description");
		task.setStatus(status);
		task.setComletedAt(completedAt);
		task.setCreatedAt(createdAt);
		task.setUser(user);
		return entityManager.persistFlushFind(task);
	}

	@Test
	void findByUser_WithZeroTasks_EmptyList() {
		List<Task> tasks = taskRepository.findByUser(anotherUser);
		assertTrue(tasks.isEmpty());
	}

	@Test
	void findByUser_WithSomeTasks_TaskList() {
		createTask("anotherTask", anotherUser, TaskStatus.IN_PROGRESS, null, LocalDateTime.now().minusHours(2));

		List<Task> foundTasks = taskRepository.findByUser(testUser);
		assertThat(foundTasks)
				.hasSize(5)
				.allMatch(task -> task.getUser() == testUser);
	}

	@Test
	void testFindTopCompletedUserTaskFromDate() {
		LocalDateTime fromDate = LocalDateTime.now().minusDays(2);
		List<Task> tasks = taskRepository.findTopCompletedUserTaskFromDate(testUser.getId(), fromDate, 5);

		assertThat(tasks).hasSize(2);
		assertThat(tasks).allMatch(
				task -> task.getStatus() == TaskStatus.COMPLETE && task.getComletedAt().isAfter(fromDate) || task.getComletedAt().isEqual(fromDate));
	}

	@Test
	void testFindTopInProgressUserTask() {
		List<Task> tasks = taskRepository.findTopInProgressUserTask(testUser.getId(), 1);

		assertThat(tasks).hasSize(1);
		assertThat(tasks.get(0).getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
	}
}
