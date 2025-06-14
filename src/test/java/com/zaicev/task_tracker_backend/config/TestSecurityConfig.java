package com.zaicev.task_tracker_backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@TestConfiguration
public class TestSecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
				.httpBasic(Customizer.withDefaults())
				.authorizeHttpRequests(authorizeHttpRequest -> authorizeHttpRequest
						.requestMatchers("/auth/**", "/error")
						.permitAll()
						.anyRequest()
						.authenticated())
				.sessionManagement(sessionManagement -> sessionManagement
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.logout(logout -> logout
						.logoutUrl("/auth/logout"))
				.csrf(csrf -> csrf
						.csrfTokenRepository(new CookieCsrfTokenRepository())
						.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
						.sessionAuthenticationStrategy(((authentication, request, response) -> {
						})));

		return httpSecurity.build();
	}
}
