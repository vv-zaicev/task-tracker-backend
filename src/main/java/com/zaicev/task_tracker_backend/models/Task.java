package com.zaicev.task_tracker_backend.models;

import jakarta.persistence.Entity;
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
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
}
