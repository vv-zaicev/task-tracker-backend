package com.zaicev.task_tracker_backend.cookie;

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

import lombok.Setter;

public class TokenCookieAuthenticationConfigurer extends AbstractHttpConfigurer<TokenCookieAuthenticationConfigurer, HttpSecurity> {

	private Function<String, Token> tokenCookieStringDeserializer;

	@Override
	public void init(HttpSecurity builder) throws Exception {
		builder.logout(logout -> logout
				.addLogoutHandler(new CookieClearingLogoutHandler("__Host-auth-token")));
	}

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		var cookieAuthenticationFilter = new AuthenticationFilter(builder.getSharedObject(AuthenticationManager.class),
				new TokenCookieAuthenticationConverter(this.tokenCookieStringDeserializer));

		cookieAuthenticationFilter.setSuccessHandler((request, response, authentication) -> {});
		cookieAuthenticationFilter.setFailureHandler(new AuthenticationEntryPointFailureHandler(new Http403ForbiddenEntryPoint()));
		
		var authenticationProvider = new PreAuthenticatedAuthenticationProvider();
		authenticationProvider.setPreAuthenticatedUserDetailsService(new TokenAuthenticationUserDetailsService());
		
		builder.addFilterAfter(cookieAuthenticationFilter, CsrfFilter.class).authenticationProvider(authenticationProvider);
	}

	public TokenCookieAuthenticationConfigurer tokenCookieStringDeserializer(Function<String, Token> tokenCookieStringDeserializer) {
		this.tokenCookieStringDeserializer = tokenCookieStringDeserializer;
		return this;
	}
	
	

}
