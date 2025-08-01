package com.zaicev.task_tracker_backend.services;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtBlacklistService jwtBlacklistService;

	@Mock
	private UserDTOConverter userDTOConverter;

	@InjectMocks
	private UserService userService;

	private User user1 = User.builder().id(1L).build();
	private User user2 = User.builder().id(2L).build();

	private UserResponseDTO userResponse1 = new UserResponseDTO(1L, null, null);
	private UserResponseDTO userResponse2 = new UserResponseDTO(2L, null, null);

	private final String validJson = "{\"email\":\"test@example.com\",\"username\":\"testuser\"}";
	private final Token validToken = new Token(UUID.randomUUID(), validJson, List.of("ROLE_USER"), Instant.now(), Instant.now());
	private final Token invalidToken = new Token(UUID.randomUUID(), "invalid json", List.of("ROLE_USER"), Instant.now(), Instant.now());

	@Test
	void loadUserByUsername_UserExists_ReturnsUserDetails() {
		String email = "test@example.com";
		User mockUser = new User();
		mockUser.setEmail(email);
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

		UserDetails userDetails = userService.loadUserByUsername(email);

		assertNotNull(userDetails);
		assertEquals(email, ((User) userDetails).getEmail());
		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	void loadUserByUsername_UserNotFound_ThrowsException() {
		String email = "missing@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(email));

		assertEquals("user with missing@example.com email is not found", exception.getMessage());
		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	void loadUserDetails_ValidToken_ReturnsUserDetails() throws Exception {
		PreAuthenticatedAuthenticationToken authToken = new PreAuthenticatedAuthenticationToken(validToken, null);
		when(jwtBlacklistService.isBlacklisted(validToken)).thenReturn(false);

		UserDetails userDetails = userService.loadUserDetails(authToken);

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
	void loadUserDetails_ValidTokenIsBlacklisted_ReturnsDisabledUserDetails() throws Exception {
		PreAuthenticatedAuthenticationToken authToken = new PreAuthenticatedAuthenticationToken(validToken, null);
		when(jwtBlacklistService.isBlacklisted(validToken)).thenReturn(true);

		UserDetails userDetails = userService.loadUserDetails(authToken);

		assertNotNull(userDetails);
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("test@example.com", ((User) userDetails).getEmail());
		assertEquals("nopassword", userDetails.getPassword());
		assertFalse(userDetails.isEnabled());
		assertTrue(userDetails.getAuthorities()
				.stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
	}

	@Test
	void loadUserDetails_InvalidPrincipalType_ThrowsException() {
		PreAuthenticatedAuthenticationToken authToken = new PreAuthenticatedAuthenticationToken("invalid_principal", null);

		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.loadUserDetails(authToken));
		assertEquals("Principal must be of type Token", exception.getMessage());
	}

	@Test
	void loadUserDetails_JsonProcessingError_ThrowsException() throws Exception {
		PreAuthenticatedAuthenticationToken authToken = new PreAuthenticatedAuthenticationToken(invalidToken, null);

		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.loadUserDetails(authToken));
		assertEquals("Invalid json token subject", exception.getMessage());
	}

	@Test
	void getUsers_ShouldReturnListOfUserResponseDTOs() {
		List<UserResponseDTO> expectedDTOs = List.of(userResponse1, userResponse2);

		when(userRepository.findAll()).thenReturn(List.of(user1, user2));
		when(userDTOConverter.toDTO(user1)).thenReturn(userResponse1);
		when(userDTOConverter.toDTO(user2)).thenReturn(userResponse2);

		List<UserResponseDTO> result = userService.getUsers();

		assertEquals(expectedDTOs, result);
	}
}
