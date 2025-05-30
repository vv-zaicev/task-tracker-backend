package com.zaicev.task_tracker_backend.exceptions;

public class EmailAlreadyTakenException extends SecurityException{

	public EmailAlreadyTakenException(String email) {
		super("user with this email: %s already exists".formatted(email));
	}
}
