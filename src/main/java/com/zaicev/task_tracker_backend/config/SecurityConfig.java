package com.zaicev.task_tracker_backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.zaicev.task_tracker_backend.authentication.JsonAuthenticationConfigurer;
import com.zaicev.task_tracker_backend.authentication.cookie.TokenCookieAuthenticationConfigurer;
import com.zaicev.task_tracker_backend.authentication.cookie.TokenCookieJweStringDeserializer;
import com.zaicev.task_tracker_backend.authentication.cookie.TokenCookieJweStringSerializer;
import com.zaicev.task_tracker_backend.authentication.cookie.TokenCookieSessionAuthenticationStrategy;
import com.zaicev.task_tracker_backend.converters.UserDTOConverter;

@Configuration
@EnableWebSecurity
@ComponentScan("com.zaicev.task_tracker_backend.security")
public class SecurityConfig {

	private String frontendURL;

	private UserDTOConverter userDTOConverter;

	public SecurityConfig(@Value("${FRONTEND_URL}") String frontendURL, UserDTOConverter userDTOConverter) {
		this.frontendURL = frontendURL;
		this.userDTOConverter = userDTOConverter;
	}

	@Bean
	TokenCookieJweStringSerializer tokenCookieJweStringSerializer(@Value("${jwt.cookie-token-key}") String cookieTokenKey) throws Exception {
		return new TokenCookieJweStringSerializer(new DirectEncrypter(OctetSequenceKey.parse(cookieTokenKey)));
	}

	@Bean
	TokenCookieJweStringDeserializer tokenCookieJweStringDeserializer(@Value("${jwt.cookie-token-key}") String cookieTokenKey) throws Exception {
		return new TokenCookieJweStringDeserializer(new DirectDecrypter(OctetSequenceKey.parse(cookieTokenKey)));
	}

	@Bean
	JsonAuthenticationConfigurer jsonAuthenticationConfigurer(TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy) {
		return new JsonAuthenticationConfigurer("/auth/sign-in", tokenCookieSessionAuthenticationStrategy, userDTOConverter);
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

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(frontendURL));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
