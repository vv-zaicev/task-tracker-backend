package com.zaicev.task_tracker_backend.exceptions;

public class UserNotFoundException extends Exception {

	public UserNotFoundException(String email) {
		super("user with this email: %s not found".formatted(email));
	}
}
