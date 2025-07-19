package com.zaicev.task_tracker_backend.converters;

import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignInRequest;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
import com.zaicev.task_tracker_backend.models.User;

public interface UserDTOConverter {
	default public User toEntity(UserSignUpRequestDTO userRequestDTO) {
		User user = new User();
		user.setUsername(userRequestDTO.username());
		user.setEmail(userRequestDTO.email());
		user.setPassword(userRequestDTO.password());
		return user;
	};
	
	default public User toEntity(UserSignInRequest userRequestDTO) {
		User user = new User();
		user.setEmail(userRequestDTO.email());
		user.setPassword(userRequestDTO.password());
		return user;
	};
	
	default public UserResponseDTO toDTO(User user) {
		return new UserResponseDTO(user.getId() ,user.getUsername(), user.getEmail());
	};
}
