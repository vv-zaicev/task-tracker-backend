package com.zaicev.task_tracker_backend.exceptions;

public class AccountIsAlredyVerified extends Exception {

	public AccountIsAlredyVerified(String email) {
		super("user with this email: %s is already verified".formatted(email));
	}
}
