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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class JsonAuthenticationFilterTest {
	private static final String LOGIN_URL = "/api/login";
	private static final String VALID_JSON = "{\"email\":\"test@example.com\",\"password\":\"password\"}";
	private static final String INVALID_JSON = "invalid-json";

	@Mock
	private static AuthenticationManager authenticationManager;

	private Authentication authenticationResult = mock(Authentication.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks
	private static JsonAuthenticationFilter filter;

	
	private MockHttpServletResponse response;
	private MockHttpServletRequest request;
	
	@BeforeEach
	void setUp() {
		request  = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	void attemptAuthentication_ValidCredentials_ReturnsAuthentication() throws Exception {
		
		request.setContent(VALID_JSON.getBytes());
		when(authenticationManager.authenticate(any())).thenReturn(authenticationResult);

		Authentication result = filter.attemptAuthentication(request, response);

		assertEquals(result, authenticationResult);
		verify(authenticationManager)
				.authenticate(argThat(token -> ((UsernamePasswordAuthenticationToken) token).getPrincipal().equals("test@example.com") &&
						((UsernamePasswordAuthenticationToken) token).getCredentials().equals("password")));
	}

	@Test
	void attemptAuthentication_InvalidJson_ThrowsIOException() throws JsonProcessingException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContent(objectMapper.writeValueAsBytes(INVALID_JSON));

		assertThatThrownBy(() -> filter.attemptAuthentication(request, response))
				.isInstanceOf(IOException.class);
	}

	@Test
	void attemptAuthentication_AuthenticationFails_ReturnsNull() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContent(VALID_JSON.getBytes(StandardCharsets.UTF_8));
		when(authenticationManager.authenticate(any())).thenReturn(null);

		Authentication result = filter.attemptAuthentication(request, response);

		assertNull(result);
	}
}
