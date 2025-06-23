package com.zaicev.task_tracker_backend.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.models.User;

@WebMvcTest(UsersController.class)
public class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserDTOConverter userDTOConverter;

	@Test
	void getMe_ShouldReturnUserData() throws Exception {
		User testUser = new User();
		testUser.setUsername("testUser");
		testUser.setEmail("test@example.com");

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				testUser,
				null,
				Collections.emptyList());

		Mockito.when(userDTOConverter.toDTO(testUser))
				.thenReturn(new UserResponseDTO("testUser", "test@example.com"));

		mockMvc.perform(get("/users/me").with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("testUser"))
				.andExpect(jsonPath("$.email").value("test@example.com"));
	}

	@Test
	void getMe_Unauthorized_ShouldReturn401() throws Exception {
		mockMvc.perform(get("/users/me"))
				.andExpect(status().isUnauthorized());
	}
}
