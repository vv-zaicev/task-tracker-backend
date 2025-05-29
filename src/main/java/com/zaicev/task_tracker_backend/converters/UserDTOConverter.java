package com.zaicev.task_tracker_backend.converters;

import com.zaicev.task_tracker_backend.dto.UserRequestDTO;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.models.User;

public interface UserDTOConverter {
	default public User toEntity(UserRequestDTO userRequestDTO) {
		User user = new User();
		user.setEmail(userRequestDTO.email());
		user.setPassword(userRequestDTO.password());
		return user;
	};
	
	default public UserResponseDTO toDTO(User user) {
		return new UserResponseDTO(user.getEmail());
	};
}
