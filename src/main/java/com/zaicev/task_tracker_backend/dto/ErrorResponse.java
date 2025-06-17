package com.zaicev.task_tracker_backend.dto;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String message, LocalDateTime timestamp) {

}
