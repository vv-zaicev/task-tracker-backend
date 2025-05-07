package com.zaicev.task_tracker_backend.security.exceptions;

public class UserNotFoundException extends SecurityException{
	
	public UserNotFoundException(String email) {
		super("user with this email: %s not found".formatted(email));
	}
}
