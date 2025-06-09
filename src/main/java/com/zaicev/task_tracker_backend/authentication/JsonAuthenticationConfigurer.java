package com.zaicev.task_tracker_backend.authentication;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import com.zaicev.task_tracker_backend.converters.UserDTOConverter;

public class JsonAuthenticationConfigurer extends AbstractHttpConfigurer<JsonAuthenticationConfigurer, HttpSecurity> {

	private String defaultFilterProcessesUrl;

	private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

	private UserDTOConverter userDTOConverter;

	public JsonAuthenticationConfigurer(String defaultFilterProcessesUrl, SessionAuthenticationStrategy sessionAuthenticationStrategy,
			UserDTOConverter userDTOConverter) {
		this.defaultFilterProcessesUrl = defaultFilterProcessesUrl;
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
		this.userDTOConverter = userDTOConverter;
	}

	@Override
	public void init(HttpSecurity builder) throws Exception {
	}

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		JsonAuthenticationFilter jsonAuthenticationFilter = new JsonAuthenticationFilter(defaultFilterProcessesUrl);
		jsonAuthenticationFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
		jsonAuthenticationFilter.setAuthenticationSuccessHandler(new JsonAuthenticationSuccessHandler(userDTOConverter));
		jsonAuthenticationFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);

		builder.addFilterBefore(jsonAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}

	public String getDefaultFilterProcessesUrl() {
		return defaultFilterProcessesUrl;
	}

}
