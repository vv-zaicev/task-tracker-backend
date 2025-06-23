package com.zaicev.task_tracker_backend.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignInRequest;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
import com.zaicev.task_tracker_backend.models.User;

@ExtendWith(MockitoExtension.class)
public class UserDTOConverterTest {
	private final UserDTOConverter converter = new UserDTOConverter() {
	};

	private final String USERNAME = "john_doe";
	private final String EMAIL = "john@example.com";
	private final String PASSWORD = "securePassword123";

	@Test
	void toEntity_fromSignUpRequest_shouldMapAllFields() {
		UserSignUpRequestDTO signUpDTO = new UserSignUpRequestDTO(USERNAME, EMAIL, PASSWORD);

		User result = converter.toEntity(signUpDTO);

		assertEquals(USERNAME, result.getUsername());
		assertEquals(EMAIL, result.getEmail());
		assertEquals(PASSWORD, result.getPassword());
	}

	@Test
	void toEntity_fromSignInRequest_shouldMapEmailAndPassword() {
		UserSignInRequest signInDTO = new UserSignInRequest(EMAIL, PASSWORD);

		User result = converter.toEntity(signInDTO);

		assertEquals(EMAIL, result.getEmail());
		assertEquals(PASSWORD, result.getPassword());
	}

	@Test
	void toDTO_shouldMapUsernameAndEmail() {
		User user = User.builder().email(EMAIL).username(USERNAME).password(PASSWORD).build();

		UserResponseDTO result = converter.toDTO(user);

		assertEquals(EMAIL, result.email());
		assertEquals(USERNAME, result.username());
	}
}
