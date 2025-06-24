package com.zaicev.task_tracker_backend.authentication.cookie;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.zaicev.task_tracker_backend.models.Token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class TokenCookieAuthenticationConverterTest {

	@Mock
	private Function<String, Token> tokenDeserializer;

	@InjectMocks
	private TokenCookieAuthenticationConverter converter;

	@Mock
	private HttpServletRequest request;

	@Mock
	private Token token;

	@Test
	void convert_shouldReturnAuthentication_whenAuthTokenCookieIsPresent() {
		String tokenString = "jwt-token";
		Cookie[] cookies = { new Cookie("__Host-auth-token", tokenString) };

		when(request.getCookies()).thenReturn(cookies);
		when(tokenDeserializer.apply(tokenString)).thenReturn(token);

		Authentication authentication = converter.convert(request);

		assertNotNull(authentication);
		assertTrue(authentication instanceof PreAuthenticatedAuthenticationToken);
		assertEquals(token, authentication.getPrincipal());
		assertEquals(tokenString, authentication.getCredentials());
	}

	@Test
	void convert_shouldReturnNull_whenAuthTokenCookieIsAbsent() {
		Cookie[] cookies = { new Cookie("other-cookie", "value") };
		when(request.getCookies()).thenReturn(cookies);

		Authentication authentication = converter.convert(request);

		assertNull(authentication);
		verifyNoInteractions(tokenDeserializer);
	}

	@Test
	void convert_shouldReturnNull_whenNoCookies() {
		when(request.getCookies()).thenReturn(null);

		Authentication authentication = converter.convert(request);

		assertNull(authentication);
		verifyNoInteractions(tokenDeserializer);
	}
}
