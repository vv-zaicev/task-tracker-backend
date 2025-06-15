package com.zaicev.task_tracker_backend.dto;

public record EmailVerificationMessage(String email, String username, String code, int expirationTimeMinutes) {

}
