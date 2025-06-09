package com.zaicev.task_tracker_backend.authentication;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
public class JsonAuthenticationFilterTest {
	private static final String LOGIN_URL = "/api/login";
	private static final String VALID_JSON = "{\"email\":\"test@example.com\",\"password\":\"password\"}";
	private static final String INVALID_JSON = "invalid-json";

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private Authentication authenticationResult;

	private JsonAuthenticationFilter filter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		filter = new JsonAuthenticationFilter(LOGIN_URL);
		filter.setAuthenticationManager(authenticationManager);

		request = new MockHttpServletRequest("POST", LOGIN_URL);
		request.setContentType("application/json");
		response = new MockHttpServletResponse();
	}

	@Test
	void attemptAuthentication_ValidCredentials_ReturnsAuthentication() throws Exception {
		request.setContent(VALID_JSON.getBytes(StandardCharsets.UTF_8));
		when(authenticationManager.authenticate(any())).thenReturn(authenticationResult);

		Authentication result = filter.attemptAuthentication(request, response);

		assertEquals(result, authenticationResult);
		verify(authenticationManager)
				.authenticate(argThat(token -> ((UsernamePasswordAuthenticationToken) token).getPrincipal().equals("test@example.com") &&
						((UsernamePasswordAuthenticationToken) token).getCredentials().equals("password")));
	}

	@Test
	void attemptAuthentication_InvalidJson_ThrowsIOException() {
		request.setContent(INVALID_JSON.getBytes(StandardCharsets.UTF_8));

		assertThatThrownBy(() -> filter.attemptAuthentication(request, response))
				.isInstanceOf(IOException.class);
	}

	@Test
	void attemptAuthentication_AuthenticationFails_ReturnsNullAndLogsError() throws Exception {
		request.setContent(VALID_JSON.getBytes(StandardCharsets.UTF_8));
		AuthenticationException authException = mock(AuthenticationException.class);
		when(authenticationManager.authenticate(any())).thenThrow(authException);
		when(authException.getMessage()).thenReturn("Invalid credentials");

		Authentication result = filter.attemptAuthentication(request, response);

		assertNull(result);
		verify(authException).getMessage();
	}
}
