package com.zaicev.task_tracker_backend.authentication.cookie;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import com.zaicev.task_tracker_backend.models.Token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;


public class TokenCookieSessionAuthenticationStrategy implements SessionAuthenticationStrategy{
	
	@Setter
	private Function<Authentication, Token> tokenCookieFactory = new DefaultTokenCookieFactory();
	
	@Setter
	private Function<Token, String> tokenStringSerializer = Object::toString;

	@Override
	public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response)
			throws SessionAuthenticationException {
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			Token token = tokenCookieFactory.apply(authentication);
			String tokenString = tokenStringSerializer.apply(token);
			
			Cookie cookie = new Cookie("__Host-auth-token", tokenString);
			cookie.setPath("/");
			cookie.setDomain(null);
			cookie.setSecure(true);
			cookie.setHttpOnly(true);
			cookie.setMaxAge((int) ChronoUnit.SECONDS.between(Instant.now(), token.expiresAt()));
			
			response.addCookie(cookie);
		}
		
		
	}
	
	
	
}
