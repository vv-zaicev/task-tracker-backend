package com.zaicev.task_tracker_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.zaicev.task_tracker_backend.authentication.JsonAuthenticationConfigurer;
import com.zaicev.task_tracker_backend.authentication.cookie.TokenCookieAuthenticationConfigurer;
import com.zaicev.task_tracker_backend.authentication.cookie.TokenCookieJweStringDeserializer;
import com.zaicev.task_tracker_backend.authentication.cookie.TokenCookieJweStringSerializer;
import com.zaicev.task_tracker_backend.authentication.cookie.TokenCookieSessionAuthenticationStrategy;

@TestConfiguration
public class TestSecurityConfig {
	@Bean
	TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer(@Value("${jwt.cookie-token-key}") String cookieTokenKey)
			throws Exception {
		return new TokenCookieAuthenticationConfigurer()
				.tokenCookieStringDeserializer(new TokenCookieJweStringDeserializer(new DirectDecrypter(OctetSequenceKey.parse(cookieTokenKey))));
	}

	@Bean
	JsonAuthenticationConfigurer jsonAuthenticationConfigurer(TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy) {
		return new JsonAuthenticationConfigurer("/auth/sign-in", tokenCookieSessionAuthenticationStrategy);
	}

	@Bean
	TokenCookieJweStringSerializer tokenCookieJweStringSerializer(@Value("${jwt.cookie-token-key}") String cookieTokenKey) throws Exception {
		return new TokenCookieJweStringSerializer(new DirectEncrypter(OctetSequenceKey.parse(cookieTokenKey)));
	}

	@Bean
	TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy(TokenCookieJweStringSerializer tokenCookieJweStringSerializer) {
		TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy = new TokenCookieSessionAuthenticationStrategy();
		tokenCookieSessionAuthenticationStrategy.setTokenStringSerializer(tokenCookieJweStringSerializer);
		return tokenCookieSessionAuthenticationStrategy;
	}
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer,
			JsonAuthenticationConfigurer jsonAuthenticationConfigurer)
			throws Exception {
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

		httpSecurity.with(tokenCookieAuthenticationConfigurer, Customizer.withDefaults());
		httpSecurity.with(jsonAuthenticationConfigurer, Customizer.withDefaults());

		return httpSecurity.build();
	}
}
