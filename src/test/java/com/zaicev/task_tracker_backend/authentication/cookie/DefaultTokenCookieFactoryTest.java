package com.zaicev.task_tracker_backend.authentication.cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.models.User;

@ExtendWith(MockitoExtension.class)
public class DefaultTokenCookieFactoryTest {
	@Mock
	private Authentication authentication;

	@Mock
	private UserDTOConverter userDTOConverter;

	@Spy
	private ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks
	private DefaultTokenCookieFactory factory;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
				.email("test@example.com")
				.username("testUser")
				.roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
				.build();
	}

	@Test
	void apply_shouldCreateValidToken() throws JsonProcessingException {
		UserResponseDTO userDTO = new UserResponseDTO("testUser", "test@example.com");
		String expectedJson = objectMapper.writeValueAsString(userDTO);
		factory.setTokenTtl(Duration.ofMinutes(15));
		when(authentication.getPrincipal()).thenReturn(testUser);
		when(userDTOConverter.toDTO(testUser)).thenReturn(userDTO);

		Instant testStart = Instant.now().minusMillis(100);
		Token token = factory.apply(authentication);
		Instant testEnd = Instant.now().plusMillis(100);

		assertNotNull(token.id());
		assertEquals(expectedJson, token.subject());
		assertTrue(CollectionUtils.isEqualCollection(Set.of("ROLE_ADMIN", "ROLE_USER"), token.authorites()));
		assertTrue(token.createdAt().isAfter(testStart) && token.createdAt().isBefore(testEnd));
		assertEquals(token.createdAt().plus(Duration.ofMinutes(15)), token.expiresAt());
	}

	@Test
	void apply_WithoutTokenTtl_ShouldExpiresAtIsAfterThanCreatedAt() {
		UserResponseDTO userDTO = new UserResponseDTO("testUser", "test@example.com");
		when(authentication.getPrincipal()).thenReturn(testUser);
		when(userDTOConverter.toDTO(testUser)).thenReturn(userDTO);

		Token token = factory.apply(authentication);

		assertTrue(token.expiresAt().isAfter(token.createdAt()));
	}
}
