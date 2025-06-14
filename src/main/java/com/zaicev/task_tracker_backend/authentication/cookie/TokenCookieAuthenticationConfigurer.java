package com.zaicev.task_tracker_backend.authentication.cookie;

import java.util.function.Function;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;

import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.services.TokenAuthenticationUserDetailsService;

import jakarta.servlet.http.Cookie;
import lombok.Setter;

public class TokenCookieAuthenticationConfigurer extends AbstractHttpConfigurer<TokenCookieAuthenticationConfigurer, HttpSecurity> {

	private final Function<String, Token> tokenCookieStringDeserializer;

	private final TokenAuthenticationUserDetailsService tokenAuthenticationUserDetailsService;

	public TokenCookieAuthenticationConfigurer(Function<String, Token> tokenCookieStringDeserializer,
			TokenAuthenticationUserDetailsService tokenAuthenticationUserDetailsService) {
		this.tokenCookieStringDeserializer = tokenCookieStringDeserializer;
		this.tokenAuthenticationUserDetailsService = tokenAuthenticationUserDetailsService;
	}

	@Override
	public void init(HttpSecurity builder) throws Exception {
		Cookie cookie = new Cookie("__Host-auth-token", null);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setDomain(null);
		builder.logout(logout -> logout
				.addLogoutHandler(new CookieClearingLogoutHandler(cookie)));
	}

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		var cookieAuthenticationFilter = new AuthenticationFilter(builder.getSharedObject(AuthenticationManager.class),
				new TokenCookieAuthenticationConverter(this.tokenCookieStringDeserializer));

		cookieAuthenticationFilter.setSuccessHandler((request, response, authentication) -> {
		});
		cookieAuthenticationFilter.setFailureHandler(new AuthenticationEntryPointFailureHandler(new Http403ForbiddenEntryPoint()));

		var authenticationProvider = new PreAuthenticatedAuthenticationProvider();
		authenticationProvider.setPreAuthenticatedUserDetailsService(tokenAuthenticationUserDetailsService);

		builder.addFilterAfter(cookieAuthenticationFilter, CsrfFilter.class).authenticationProvider(authenticationProvider);
	}

}
