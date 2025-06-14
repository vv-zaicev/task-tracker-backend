package com.zaicev.task_tracker_backend.authentication.cookie;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.services.JwtBlacklistService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtBlacklistLogoutHandler implements LogoutHandler {
	
	private final JwtBlacklistService jwtBlacklistService;
	
	private final Function<String, Token> tokenCookieStringDeserializer;

	public JwtBlacklistLogoutHandler(JwtBlacklistService jwtBlacklistService, Function<String, Token> tokenCookieStringDeserializer) {
		this.jwtBlacklistService = jwtBlacklistService;
		this.tokenCookieStringDeserializer = tokenCookieStringDeserializer;
	}

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {	
		Optional<Cookie> jwtCookie =  Stream.of(request.getCookies()).filter(cookie -> cookie.getName().equals("__Host-auth-token")).findFirst();
		if (jwtCookie.isPresent()) {
			Token token = tokenCookieStringDeserializer.apply(jwtCookie.get().getValue());
			jwtBlacklistService.addTokenToBlacklist(token);
		}
	}

}
