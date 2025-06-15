package com.zaicev.task_tracker_backend.dto;

public record MailVerificationMessage(String email, String username, String code, int expirationTimeMinutes) {

}
