package com.zaicev.task_tracker_backend.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import com.zaicev.task_tracker_backend.services.UserService;

@WebMvcTest(UsersController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserDTOConverter userDTOConverter;

	@MockitoBean
	private UserService userService;

	private User testUser;

	private UserResponseDTO userResponse = new UserResponseDTO(1L, "testUser", "test@example.com");

	private Authentication authentication;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setUsername("testUser");
		testUser.setEmail("test@example.com");

		authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
	}

	@Test
	void getUsers_ShouldReturnUserList() throws Exception {
		when(userService.getUsers()).thenReturn(List.of(userResponse));

		mockMvc.perform(get("/users").with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(userResponse.id()))
				.andExpect(jsonPath("$[0].username").value(userResponse.username()))
				.andExpect(jsonPath("$[0].email").value(userResponse.email()));
	}

	@Test
	void getUsers_Unauthorized_ShouldReturn401() throws Exception {
		mockMvc.perform(get("/users"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getMe_ShouldReturnUserData() throws Exception {
		Mockito.when(userDTOConverter.toDTO(testUser))
				.thenReturn(userResponse);

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
