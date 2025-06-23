package com.zaicev.task_tracker_backend.services;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.zaicev.task_tracker_backend.dto.EmailVerificationMessage;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
import com.zaicev.task_tracker_backend.dto.VerifyUserDTO;
import com.zaicev.task_tracker_backend.exceptions.AccountIsAlredyVerified;
import com.zaicev.task_tracker_backend.exceptions.InvalidVerificationCode;
import com.zaicev.task_tracker_backend.exceptions.UserNotFoundException;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

	private int expirationTimeMinutes = 15;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private KafkaProducer kafkaProducer;

	@InjectMocks
	private SecurityService securityService;

	@BeforeEach
	void setup() {
		securityService.setExpirationTimeMinutes(expirationTimeMinutes);
	}

	@Test
	void signup_shouldSaveUserAndSendVerificationMessage() {
		UserSignUpRequestDTO request = new UserSignUpRequestDTO("testuser", "test@mail.com", "password");
		when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		securityService.signup(request);

		verify(userRepository).save(userCaptor.capture());
		verify(kafkaProducer).sendMessage(any(EmailVerificationMessage.class));

		User savedUser = userCaptor.getValue();
		assertEquals("testuser", savedUser.getUsername());
		assertEquals("test@mail.com", savedUser.getEmail());
		assertFalse(savedUser.isEnabled());
		assertNotNull(savedUser.getVerificationCode());
		assertNotNull(savedUser.getVerificationCodeExpiresAt());
	}

	@Test
	void verifyUser_shouldEnableUser_whenCodeIsValidAndNotExpired() throws Exception {
		String code = "123456";
		User user = new User();
		user.setEmail("user@mail.com");
		user.setVerificationCode(code);
		user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
		user.setEnabled(false);

		when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

		VerifyUserDTO dto = new VerifyUserDTO("user@mail.com", code);

		securityService.verifyUser(dto);

		assertTrue(user.isEnabled());
		assertNull(user.getVerificationCode());
		assertNull(user.getVerificationCodeExpiresAt());
		verify(userRepository).save(user);
	}

	@Test
	void verifyUser_shouldThrowInvalidVerificationCode_whenCodeIsExpired() {
		User user = new User();
		user.setEmail("user@mail.com");
		user.setVerificationCode("123456");
		user.setVerificationCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
		user.setEnabled(false);

		when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

		VerifyUserDTO dto = new VerifyUserDTO("user@mail.com", "123456");

		assertThrows(InvalidVerificationCode.class, () -> securityService.verifyUser(dto));
	}

	@Test
	void verifyUser_shouldThrowInvalidVerificationCode_whenCodeIsIncorrect() {
		User user = new User();
		user.setEmail("user@mail.com");
		user.setVerificationCode("123456");
		user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
		user.setEnabled(false);

		when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

		VerifyUserDTO dto = new VerifyUserDTO("user@mail.com", "000000");

		assertThrows(InvalidVerificationCode.class, () -> securityService.verifyUser(dto));
	}

	@Test
	void resendVerificationCode_shouldSendNewCode_whenUserNotVerified() throws Exception {
		User user = new User();
		user.setEmail("user@mail.com");
		user.setUsername("testuser");
		user.setEnabled(false);

		when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

		securityService.resendVerificationCode("user@mail.com");

		verify(userRepository).save(user);
		verify(kafkaProducer).sendMessage(any(EmailVerificationMessage.class));
		assertNotNull(user.getVerificationCode());
		assertNotNull(user.getVerificationCodeExpiresAt());
	}

	@Test
	void resendVerificationCode_shouldThrowException_whenUserAlreadyVerified() {
		User user = new User();
		user.setEmail("user@mail.com");
		user.setEnabled(true);

		when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

		assertThrows(AccountIsAlredyVerified.class, () -> securityService.resendVerificationCode("user@mail.com"));
	}

	@Test
	void verifyUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
		when(userRepository.findByEmail("notfound@mail.com")).thenReturn(Optional.empty());

		VerifyUserDTO dto = new VerifyUserDTO("notfound@mail.com", "123456");

		assertThrows(UserNotFoundException.class, () -> securityService.verifyUser(dto));
	}

	@Test
	void resendVerificationCode_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
		when(userRepository.findByEmail("notfound@mail.com")).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> securityService.resendVerificationCode("notfound@mail.com"));
	}
}
