package com.zaicev.task_tracker_backend.controllers;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zaicev.task_tracker_backend.dto.ErrorResponse;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
import com.zaicev.task_tracker_backend.dto.VerifyUserDTO;
import com.zaicev.task_tracker_backend.exceptions.AccountIsAlredyVerified;
import com.zaicev.task_tracker_backend.exceptions.InvalidVerificationCode;
import com.zaicev.task_tracker_backend.exceptions.UserNotFoundException;
import com.zaicev.task_tracker_backend.services.SecurityService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class SecurityController {

	private final CsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();

	private final SecurityService securityService;

	public SecurityController(SecurityService securityService) {
		this.securityService = securityService;
	}

	@GetMapping("/csrf")
	public CsrfToken getCsrfToken(HttpServletRequest request, HttpServletResponse response) {
		var token = csrfTokenRepository.loadToken(request);
		if (token == null) {
			token = csrfTokenRepository.generateToken(request);
			csrfTokenRepository.saveToken(token, request, response);
		}

		return token;
	}

	@PostMapping("/sign-up")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponseDTO signup(@RequestBody UserSignUpRequestDTO userRequestDTO) {
		return securityService.signup(userRequestDTO);
	}

	@PostMapping("/verify")
	public void verifyUser(@RequestBody VerifyUserDTO verifyUserDTO) throws InvalidVerificationCode, UserNotFoundException {
		securityService.verifyUser(verifyUserDTO);
	}

	@PostMapping("/resend")
	public void resendVerificationCode(@RequestParam String email) throws UserNotFoundException, AccountIsAlredyVerified {
		securityService.resendVerificationCode(email);
	}

	@ExceptionHandler({ InvalidVerificationCode.class, AccountIsAlredyVerified.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleInvalidRequests(Exception ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now());
		return errorResponse;
	}

	@ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
		return errorResponse;
	}
}
