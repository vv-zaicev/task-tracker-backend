package com.zaicev.task_tracker_backend.cookie;

import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.zaicev.task_tracker_backend.models.Token;

import jakarta.servlet.http.HttpServletRequest;

public class TokenCookieAuthenticationConverter implements AuthenticationConverter {

	private final Function<String, Token> tokenCookieStringDeserializer;

	public TokenCookieAuthenticationConverter(Function<String, Token> tokenCookieStringDeserializer) {
		this.tokenCookieStringDeserializer = tokenCookieStringDeserializer;
	}

	@Override
	public Authentication convert(HttpServletRequest request) {
		if (request.getCookies() != null) {
			return Stream.of(request.getCookies())
					.filter(cookie -> cookie.getName().equals("__Host-auth-token"))
					.findFirst()
					.map(cookie -> {
						Token token = this.tokenCookieStringDeserializer.apply(cookie.getValue());
						return new PreAuthenticatedAuthenticationToken(token, cookie.getValue());
					})
					.orElse(null);
		}
		return null;
	}

}
