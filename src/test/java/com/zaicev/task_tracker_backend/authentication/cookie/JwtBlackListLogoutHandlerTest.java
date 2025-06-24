package com.zaicev.task_tracker_backend.authentication.cookie;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.services.JwtBlacklistService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class JwtBlackListLogoutHandlerTest {

	@Mock
	private JwtBlacklistService jwtBlacklistService;

	@Mock
	private Function<String, Token> tokenDeserializer;

	@InjectMocks
	private JwtBlacklistLogoutHandler jwtBlacklistLogoutHandler;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Authentication authentication;

	@Test
	void logout_jwtCookieIsPresent_shouldAddTokenToBlacklist() {
		String jwt = "jwtTokenValue";
		Cookie[] cookies = {
				new Cookie("__Host-auth-token", jwt)
		};
		Token token = new Token(UUID.randomUUID(), "subject", Collections.emptyList(), Instant.now(), Instant.now());

		when(request.getCookies()).thenReturn(cookies);
		when(tokenDeserializer.apply(jwt)).thenReturn(token);

		jwtBlacklistLogoutHandler.logout(request, response, authentication);

		verify(tokenDeserializer).apply(jwt);
		verify(jwtBlacklistService).addTokenToBlacklist(token);
	}

	@Test
	void logout_JwtCookieIsNotPresent_shouldDoNothing() {
		Cookie[] cookies = {
				new Cookie("other-cookie", "value")
		};

		when(request.getCookies()).thenReturn(cookies);

		jwtBlacklistLogoutHandler.logout(request, response, authentication);

		verifyNoInteractions(tokenDeserializer);
		verifyNoInteractions(jwtBlacklistService);
	}

	@Test
	void logout_RequestHasNoCookies_shouldDoNothing() {
		when(request.getCookies()).thenReturn(null);

		jwtBlacklistLogoutHandler.logout(request, response, authentication);

		verifyNoInteractions(tokenDeserializer);
		verifyNoInteractions(jwtBlacklistService);
	}
}
