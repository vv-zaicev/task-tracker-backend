package com.zaicev.task_tracker_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserDetilasServiceImplTest {
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void loadUserByUsername_UserExists_ReturnsUserDetails() {
		String email = "test@example.com";
		User mockUser = new User();
		mockUser.setEmail(email);
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

		UserDetails userDetails = userDetailsService.loadUserByUsername(email);

		assertNotNull(userDetails);
		assertEquals(email, ((User) userDetails).getEmail());
		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	void loadUserByUsername_UserNotFound_ThrowsException() {
		String email = "missing@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));

		assertEquals("user with missing@example.com email is not found", exception.getMessage());
		verify(userRepository, times(1)).findByEmail(email);
	}
}
