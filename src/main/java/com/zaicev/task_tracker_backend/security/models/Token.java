package com.zaicev.task_tracker_backend.security.models;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Token(UUID id, String subject, List<String> authorites, Instant createdAt, Instant expiresAt) {

}
