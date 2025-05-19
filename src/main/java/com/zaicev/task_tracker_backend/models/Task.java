package com.zaicev.task_tracker_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
	@Id
	private Long id;
	
	private String title;
	
	private String description;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Enumerated(EnumType.STRING)
	private TaskStatus status;
	
	@ManyToOne
	@JoinColumn(name = "owner_id")
	private User user;
}
