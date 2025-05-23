package com.zaicev.task_tracker_backend.models;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String email;

	private String password;

	@Builder.Default
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = {@JoinColumn(name = "user_id")})
	@Column(name = "role")
	private Set<String> roles = new HashSet<>();

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof User))
			return false;
		User other = (User) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	
}
