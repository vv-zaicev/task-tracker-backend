package com.zaicev.task_tracker_backend.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.zaicev.task_tracker_backend.converters.DefaultUserDTOConverter;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.UserRepository;

@Service
public class SecurityService {
	private final UserDTOConverter userDTOConverter = new DefaultUserDTOConverter();

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public SecurityService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public UserResponseDTO signup(UserSignUpRequestDTO userSignUpRequestDTO) {
		User user = userDTOConverter.toEntity(userSignUpRequestDTO);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(true);
		userRepository.save(user);
		return userDTOConverter.toDTO(user);
	}
}
