package com.zaicev.task_tracker_backend.controllers;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaicev.task_tracker_backend.config.TestSecurityConfig;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
import com.zaicev.task_tracker_backend.dto.VerifyUserDTO;
import com.zaicev.task_tracker_backend.exceptions.AccountIsAlredyVerified;
import com.zaicev.task_tracker_backend.exceptions.InvalidVerificationCode;
import com.zaicev.task_tracker_backend.exceptions.UserNotFoundException;
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

	@BeforeEach
	void initialize() {
		existingToken = csrfTokenRepository.generateToken(new MockHttpServletRequest());
		csrfCookie = new Cookie("XSRF-TOKEN", existingToken.getToken());
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
		UserResponseDTO responseDto = new UserResponseDTO(1L, "username", "user@example.com");

		when(securityService.signup(any(UserSignUpRequestDTO.class))).thenReturn(responseDto);

		mockMvc.perform(post("/auth/sign-up")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$", aMapWithSize(3)))
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.username").value("username"))
				.andExpect(jsonPath("$.id").value(1L));
		
	}

	@Test
	void signUp_WithoutCsrf_shouldReturnUnauthorized() throws Exception {
		UserSignUpRequestDTO requestDto = new UserSignUpRequestDTO("username", "user@example.com", "password123");

		mockMvc.perform(post("/auth/sign-up")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void verify_WithoutCsrf_shouldReturnUnauthorized() throws Exception {
		VerifyUserDTO verifyUserDTO = new VerifyUserDTO("user@example.com", "000000");

		mockMvc.perform(post("/auth/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyUserDTO)))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void verify_CorrectData_shouldReturnOK() throws Exception {
		VerifyUserDTO verifyUserDTO = new VerifyUserDTO("user@example.com", "000000");

		mockMvc.perform(post("/auth/verify")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyUserDTO)))
				.andExpect(status().isOk());
		
		verify(securityService).verifyUser(verifyUserDTO);
	}
	
	@Test
	void verify_IncorrectCode_shouldReturnBadRequest() throws Exception {
		VerifyUserDTO verifyUserDTO = new VerifyUserDTO("user@example.com", "000000");
		Exception invalidVerificationCode = new InvalidVerificationCode("invalid code");
		doThrow(invalidVerificationCode).when(securityService).verifyUser(verifyUserDTO);
		
		mockMvc.perform(post("/auth/verify")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyUserDTO)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.message").value(invalidVerificationCode.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());
		
	}
	
	@Test
	void verify_IncorrectUser_shouldReturnNotFound() throws Exception {
		VerifyUserDTO verifyUserDTO = new VerifyUserDTO("user@example.com", "000000");
		Exception userNotFoundException = new UserNotFoundException(verifyUserDTO.email());
		doThrow(userNotFoundException).when(securityService).verifyUser(verifyUserDTO);
		
		mockMvc.perform(post("/auth/verify")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyUserDTO)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(userNotFoundException.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());
		
	}
	
	
	@Test
	void resend_WithoutCsrf_shouldReturnUnauthorized() throws Exception {

		mockMvc.perform(post("/auth/resend")
				.param("email", "user@example.com"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void resend_CorrectData_shouldReturnOK() throws Exception {

		mockMvc.perform(post("/auth/resend")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.param("email", "user@example.com"))
				.andExpect(status().isOk());
		
		verify(securityService).resendVerificationCode("user@example.com");
	}
	
	@Test
	void resend_AccountIsAlredyVerified_shouldReturnBadRequest() throws Exception {
		String email = "user@example.com";
		Exception accountIsAlredyVerified = new AccountIsAlredyVerified(email);
		doThrow(accountIsAlredyVerified).when(securityService).resendVerificationCode(email);;
		
		mockMvc.perform(post("/auth/resend")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.param("email", "user@example.com"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.message").value(accountIsAlredyVerified.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());
		
	}
	
	@Test
	void resend_IncorrectUser_shouldReturnNotFound() throws Exception {
		String email = "user@example.com";
		Exception userNotFoundException = new UserNotFoundException(email);
		doThrow(userNotFoundException).when(securityService).resendVerificationCode(email);;
		
		mockMvc.perform(post("/auth/resend")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.param("email", "user@example.com"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
				.andExpect(jsonPath("$.message").value(userNotFoundException.getMessage()))
				.andExpect(jsonPath("$.timestamp").exists());
		
	}
}
