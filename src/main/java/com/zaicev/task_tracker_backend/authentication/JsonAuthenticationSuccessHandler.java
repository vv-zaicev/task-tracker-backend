package com.zaicev.task_tracker_backend.authentication;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private UserDTOConverter userDTOConverter = new UserDTOConverter() {
	};
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JsonAuthenticationSuccessHandler(UserDTOConverter userDTOConverter) {
		super();
		this.userDTOConverter = userDTOConverter;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		User user = (User) authentication.getPrincipal();

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");

		response.getWriter().write(objectMapper.writeValueAsString(userDTOConverter.toDTO(user)));
	}

}
