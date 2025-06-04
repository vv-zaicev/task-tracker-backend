package com.zaicev.task_tracker_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

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
	}

	@Test
	void findByUser_WithZeroTasks_EmptyList() {
		List<Task> tasks = taskRepository.findByUser(testUser);
		assertTrue(tasks.isEmpty());
	}

	@Test
	void findByUser_WithSomeTasks_TaskList() {
		Task task1 = createTaskForUser("Task 1", testUser);
		Task task2 = createTaskForUser("Task 2", testUser);
		createTaskForUser("Another User Task", anotherUser);

		List<Task> foundTasks = taskRepository.findByUser(testUser);
		assertThat(foundTasks)
				.hasSize(2)
				.extracting(Task::getTitle)
				.containsExactlyInAnyOrder(task1.getTitle(), task2.getTitle());
	}

	private Task createTaskForUser(String title, User user) {
		Task task = new Task();
		task.setTitle(title);
		task.setDescription("Test Description");
		task.setStatus(TaskStatus.IN_PROGRESS);
		task.setUser(user);
		return entityManager.persistFlushFind(task);
	}
}
