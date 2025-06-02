package com.zaicev.task_tracker_backend.authentication.cookie;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.models.User;

import lombok.Setter;

public class DefaultTokenCookieFactory implements Function<Authentication, Token>{
	
	@Setter
	private Duration tokenTtl = Duration.ofMinutes(10);

	@Override
	public Token apply(Authentication authentication) {
		User user = (User) authentication.getPrincipal();
		var now = Instant.now();
		return new Token(UUID.randomUUID(), user.getEmail(), user.getAuthorities()
										.stream()
										.map(GrantedAuthority::getAuthority).toList(), now, now.plus(tokenTtl));
	}	

}
