package com.zaicev.task_tracker_backend;

import org.springframework.boot.SpringApplication;

public class TestTaskTrackerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskTrackerBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
