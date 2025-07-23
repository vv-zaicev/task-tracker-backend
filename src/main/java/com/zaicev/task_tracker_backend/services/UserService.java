package com.zaicev.task_tracker_backend.services;

import java.util.HashSet;
import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;
import com.zaicev.task_tracker_backend.dto.UserResponseDTO;
import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.models.User;
import com.zaicev.task_tracker_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	private final UserRepository userRepository;
	
	private final UserDTOConverter userDTOConverter;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final JwtBlacklistService jwtBlacklistService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("user with %s email is not found".formatted(username)));
		return user;
	}

	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken) throws UsernameNotFoundException {
		if (authenticationToken.getPrincipal() instanceof Token token) {
			try {
				UserResponseDTO userResponseDTO = objectMapper.readValue(token.subject(), UserResponseDTO.class);
				User user = User.builder()
						.email(userResponseDTO.email())
						.username(userResponseDTO.username())
						.password("nopassword")
						.roles(new HashSet<String>(token.authorites()))
						.enabled(!jwtBlacklistService.isBlacklisted(token))
						.build();
				return user;
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
				throw new UsernameNotFoundException("Invalid json token subject");
			}

		}

		throw new UsernameNotFoundException("Principal must be of type Token");
	}

	public List<UserResponseDTO> getUsers() {
		return StreamSupport
				.stream(userRepository.findAll().spliterator(), false)
				.map(userDTOConverter::toDTO)
				.toList();
	}

}
