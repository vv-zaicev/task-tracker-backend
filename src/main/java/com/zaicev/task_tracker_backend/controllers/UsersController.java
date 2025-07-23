package com.zaicev.task_tracker_backend.controllers;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UsersController {

	private final UserDTOConverter userDTOConverter;

	private final UserService userService;

	@GetMapping()
	public List<UserResponseDTO> getUsers() {
		return userService.getUsers();
	}

	@GetMapping("/me")
	public UserResponseDTO getMe(@AuthenticationPrincipal User user) {
		return userDTOConverter.toDTO(user);
	}

}
