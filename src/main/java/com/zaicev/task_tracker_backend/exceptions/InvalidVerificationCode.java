package com.zaicev.task_tracker_backend.exceptions;

public class InvalidVerificationCode extends SecurityException{

	public InvalidVerificationCode(String message) {
		super(message);
	}

}
