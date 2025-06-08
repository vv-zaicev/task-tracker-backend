package com.zaicev.task_tracker_backend.authentication.cookie;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.models.User;

import lombok.Setter;

public class DefaultTokenCookieFactory implements Function<Authentication, Token>{
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Setter
	private UserDTOConverter userDTOConverter = new UserDTOConverter() {};
	
	@Setter
	private Duration tokenTtl = Duration.ofMinutes(10);

	@Override
	public Token apply(Authentication authentication) {
		User user = (User) authentication.getPrincipal();
		var now = Instant.now();
		try {
			return new Token(UUID.randomUUID(), objectMapper.writeValueAsString(userDTOConverter.toDTO(user)), user.getAuthorities()
											.stream()
											.map(GrantedAuthority::getAuthority).toList(), now, now.plus(tokenTtl));
		} catch (JsonProcessingException e) {
			throw new SessionAuthenticationException(e.getMessage());
		}
	}	

}
