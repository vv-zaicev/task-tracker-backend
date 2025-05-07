package com.zaicev.task_tracker_backend.security.exceptions;

public class EmailAlreadyTakenException extends SecurityException{

	public EmailAlreadyTakenException(String email) {
		super("user with this email: %s already exists".formatted(email));
	}
}
