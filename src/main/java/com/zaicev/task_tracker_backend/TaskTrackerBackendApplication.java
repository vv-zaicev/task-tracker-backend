package com.zaicev.task_tracker_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.zaicev.task_tracker_backend.converters.TaskDTOConverter;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;

@SpringBootApplication
public class TaskTrackerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskTrackerBackendApplication.class, args);
	}

	@Bean
	TaskDTOConverter defaultTaskDTOConverter() {
		return new TaskDTOConverter() {};
	}
	
	@Bean
	UserDTOConverter defaultUserDTOConverter() {
		return new UserDTOConverter() {};
	}

}
