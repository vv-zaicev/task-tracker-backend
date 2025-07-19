package com.zaicev.task_tracker_backend.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.models.User;

@ExtendWith(MockitoExtension.class)
public class JsonAuthenticationSuccessHandlerTest {

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private Authentication authentication;

	private User user;

	@Mock
	private UserDTOConverter userDTOConverter;

	@InjectMocks
	private JsonAuthenticationSuccessHandler handler;

	private ObjectMapper testObjectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		user = User.builder().email("test@example.com").username("testUser").build();
		authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
	}

	@Test
	void onAuthenticationSuccess_ShouldWriteCorrectJson() throws Exception {

		UserResponseDTO expectedDTO = new UserResponseDTO(1L, "testUser", "test@example.com");
		String expectedJson = testObjectMapper.writeValueAsString(expectedDTO);

		when(userDTOConverter.toDTO(user)).thenReturn(expectedDTO);

		handler.onAuthenticationSuccess(request, response, authentication);

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON.toString(), response.getContentType());
		assertEquals(expectedJson, response.getContentAsString());
	}

}
