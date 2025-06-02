package com.zaicev.task_tracker_backend.authentication;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

public class JsonAuthenticationConfigurer extends AbstractHttpConfigurer<JsonAuthenticationConfigurer, HttpSecurity> {

	private String defaultFilterProcessesUrl;
	
	private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
	
	
	
	public JsonAuthenticationConfigurer(String defaultFilterProcessesUrl, SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		this.defaultFilterProcessesUrl = defaultFilterProcessesUrl;
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}

	@Override
	public void init(HttpSecurity builder) throws Exception {
	}

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		JsonAuthenticationFilter jsonAuthenticationFilter = new JsonAuthenticationFilter(defaultFilterProcessesUrl);
		jsonAuthenticationFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
		jsonAuthenticationFilter.setAuthenticationSuccessHandler(new JsonAuthenticationSuccessHandler());
		jsonAuthenticationFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);

		builder.addFilterBefore(jsonAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}

	public String getDefaultFilterProcessesUrl() {
		return defaultFilterProcessesUrl;
	}
	
	
}
