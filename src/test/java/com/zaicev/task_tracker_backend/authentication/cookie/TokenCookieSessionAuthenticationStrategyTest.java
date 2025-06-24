package com.zaicev.task_tracker_backend.authentication.cookie;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.zaicev.task_tracker_backend.models.Token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class TokenCookieSessionAuthenticationStrategyTest {

	@Mock
	private Function<Token, String> tokenSerializer;

	@Mock
	private Function<Authentication, Token> tokenFactory;

	@InjectMocks
	private TokenCookieSessionAuthenticationStrategy strategy;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Test
	void onAuthentication_authenticationIsUsernamePasswordToken_shouldAddCookie() {
		strategy.setTokenCookieFactory(tokenFactory);
		strategy.setTokenStringSerializer(tokenSerializer);
		
		UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);
		Token token = mock(Token.class);
		Instant expiry = Instant.now().plus(10, ChronoUnit.MINUTES);
		String serializedToken = "tokenString";

		when(tokenFactory.apply(auth)).thenReturn(token);
		when(token.expiresAt()).thenReturn(expiry);
		when(tokenSerializer.apply(token)).thenReturn(serializedToken);

		ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

		strategy.onAuthentication(auth, request, response);

		verify(response).addCookie(cookieCaptor.capture());
		Cookie cookie = cookieCaptor.getValue();

		assertEquals("__Host-auth-token", cookie.getName());
		assertEquals(serializedToken, cookie.getValue());
		assertEquals("/", cookie.getPath());
		assertNull(cookie.getDomain());
		assertTrue(cookie.isHttpOnly());
		assertTrue(cookie.getSecure());
		assertTrue(cookie.getMaxAge() > 0);
	}

	@Test
	void onAuthentication_authenticationIsNotUsernamePasswordToken_shouldDoNothing() {
		Authentication auth = mock(Authentication.class);

		strategy.onAuthentication(auth, request, response);

		verifyNoInteractions(tokenFactory);
		verifyNoInteractions(tokenSerializer);
		verifyNoInteractions(response);
	}
}
