package com.zaicev.task_tracker_backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.hamcrest.collection.IsIterableWithSize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaicev.task_tracker_backend.config.TestSecurityConfig;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
import com.zaicev.task_tracker_backend.services.SecurityService;

import jakarta.servlet.http.Cookie;

@WebMvcTest(SecurityController.class)
@Import(TestSecurityConfig.class)
public class SecurityControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private SecurityService securityService;

	private final CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
	
	private CsrfToken existingToken;
	private Cookie csrfCookie;
	private HttpHeaders httpHeaders;
	
	@BeforeEach
	void initialize() {
		existingToken = csrfTokenRepository.generateToken(new MockHttpServletRequest());
		csrfCookie = new Cookie("XSRF-TOKEN", existingToken.getToken());
		httpHeaders = new HttpHeaders();
		httpHeaders.add("X-XSRF-TOKEN", existingToken.getToken());
	}

	@Test
	void getCsrfToken_WithoutExistingToken_ShouldGenerateNewToken() throws Exception {
		mockMvc.perform(get("/auth/csrf"))
				.andExpect(status().isOk())
				.andExpect(cookie().exists("XSRF-TOKEN"))
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"));
	}

	@Test
	void getCsrfToken_WithExistingToken_ShouldReturnExistingToken() throws Exception {
		

		mockMvc.perform(get("/auth/csrf").cookie(csrfCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value(existingToken.getToken()));

	}

	@Test
	void signUp_WithCsrf_shouldCreateUser() throws Exception {
		UserSignUpRequestDTO requestDto = new UserSignUpRequestDTO("username", "user@example.com", "password123");
		UserResponseDTO responseDto = new UserResponseDTO("username", "user@example.com");

		when(securityService.signup(any(UserSignUpRequestDTO.class))).thenReturn(responseDto);

		mockMvc.perform(post("/auth/sign-up").cookie(csrfCookie).headers(httpHeaders)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$", aMapWithSize(2)))
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.username").value("username"));
	}
	
	@Test
	void signUp_WithoutCsrf_shouldReturnUnauthorized() throws Exception {
		UserSignUpRequestDTO requestDto = new UserSignUpRequestDTO("username", "user@example.com", "password123");

		mockMvc.perform(post("/auth/sign-up")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isForbidden());
	}
}
