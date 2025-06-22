package com.zaicev.task_tracker_backend.services;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.EmailVerificationMessage;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.dto.UserSignUpRequestDTO;
import com.zaicev.task_tracker_backend.dto.VerifyUserDTO;
import com.zaicev.task_tracker_backend.exceptions.AccountIsAlredyVerified;
import com.zaicev.task_tracker_backend.exceptions.InvalidVerificationCode;
import com.zaicev.task_tracker_backend.exceptions.UserNotFoundException;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityService {
	private final UserDTOConverter userDTOConverter = new UserDTOConverter() {
	};

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final KafkaProducer kafkaProducer;

	@Value("${email-verification.code.expiration-time-minutes}")
	@Setter
	private int expirationTimeMinutes;

	public UserResponseDTO signup(UserSignUpRequestDTO userSignUpRequestDTO) {
		User user = userDTOConverter.toEntity(userSignUpRequestDTO);
		String code = generateVerificationCode();

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled(false);
		user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(expirationTimeMinutes));
		user.setVerificationCode(code);

		kafkaProducer
				.sendMessage(new EmailVerificationMessage(user.getEmail(), user.getUsername(), code, expirationTimeMinutes));

		userRepository.save(user);
		return userDTOConverter.toDTO(user);
	}

	public void verifyUser(VerifyUserDTO verifyUserDTO) throws InvalidVerificationCode, UserNotFoundException {
		User user = userRepository.findByEmail(verifyUserDTO.email()).orElseThrow(() -> new UserNotFoundException(verifyUserDTO.email()));
		if (!user.isEnabled()) {
			if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
				throw new InvalidVerificationCode("Verification code has expired");
			}
			if (!user.getVerificationCode().equals(verifyUserDTO.code())) {
				throw new InvalidVerificationCode("Incorrect code");
			}

			user.setEnabled(true);
			user.setVerificationCode(null);
			user.setVerificationCodeExpiresAt(null);

			userRepository.save(user);
		}
	}

	public void resendVerificationCode(String email) throws AccountIsAlredyVerified, UserNotFoundException {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
		if (user.isEnabled()) {
			throw new AccountIsAlredyVerified(email);
		}
		String code = generateVerificationCode();

		user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(expirationTimeMinutes));
		user.setVerificationCode(code);

		kafkaProducer
				.sendMessage(new EmailVerificationMessage(user.getEmail(), user.getUsername(), code, expirationTimeMinutes));

		userRepository.save(user);
	}

	private String generateVerificationCode() {
		Random random = new Random();
		int code = random.nextInt(900000) + 100000;
		return String.valueOf(code);
	}
}
