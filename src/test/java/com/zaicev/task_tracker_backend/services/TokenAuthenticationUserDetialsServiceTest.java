package com.zaicev.task_tracker_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.models.User;

@ExtendWith(MockitoExtension.class)
public class TokenAuthenticationUserDetialsServiceTest {
	private TokenAuthenticationUserDetailsService service = new TokenAuthenticationUserDetailsService();

	private final String validJson = "{\"email\":\"test@example.com\",\"username\":\"testuser\"}";
	private final Token validToken = new Token(UUID.randomUUID(), validJson, List.of("ROLE_USER"), Instant.now(), Instant.now());
	private final Token invalidToken = new Token(UUID.randomUUID(), "invalid json", List.of("ROLE_USER"), Instant.now(), Instant.now());

	@Test
	void loadUserDetails_ValidToken_ReturnsUserDetails() throws Exception {
		PreAuthenticatedAuthenticationToken authToken = new PreAuthenticatedAuthenticationToken(validToken, null);

		UserDetails userDetails = service.loadUserDetails(authToken);

		assertNotNull(userDetails);
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("test@example.com", ((User) userDetails).getEmail());
		assertEquals("nopassword", userDetails.getPassword());
		assertTrue(userDetails.isEnabled());
		assertTrue(userDetails.getAuthorities()
				.stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
	}

	@Test
	void loadUserDetails_InvalidPrincipalType_ThrowsException() {
		PreAuthenticatedAuthenticationToken authToken = new PreAuthenticatedAuthenticationToken("invalid_principal", null);

		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> service.loadUserDetails(authToken));
		assertEquals("Principal must be of type Token", exception.getMessage());
	}

	@Test
	void loadUserDetails_JsonProcessingError_ThrowsException() throws Exception {
		PreAuthenticatedAuthenticationToken authToken = new PreAuthenticatedAuthenticationToken(invalidToken, null);

		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,() -> service.loadUserDetails(authToken));
		assertEquals("Invalid json token subject", exception.getMessage());
	}
}
