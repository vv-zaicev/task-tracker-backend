package com.zaicev.task_tracker_backend.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zaicev.task_tracker_backend.converters.DefaultUserDTOConverter;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.models.User;

@RestController
@RequestMapping("/users")
public class UsersController {

	private final UserDTOConverter userDTOConverter;

	public UsersController(UserDTOConverter userDTOConverter) {
		this.userDTOConverter = userDTOConverter;
	}

	@GetMapping("/me")
	public UserResponseDTO getMe(@AuthenticationPrincipal User user) {
		return userDTOConverter.toDTO(user);
	}
}
