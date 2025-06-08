package com.zaicev.task_tracker_backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
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
}
