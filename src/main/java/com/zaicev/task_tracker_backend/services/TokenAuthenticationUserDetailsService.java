package com.zaicev.task_tracker_backend.services;

import java.util.HashSet;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.zaicev.task_tracker_backend.models.Token;
import com.zaicev.task_tracker_backend.models.User;

public class TokenAuthenticationUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken) throws UsernameNotFoundException {
		if (authenticationToken.getPrincipal() instanceof Token token) {
			User user = new User().builder().email(token.subject()).password("nopassword").roles(new HashSet<String>(token.authorites())).enabled(true).build();
			return user;
		}

		throw new UsernameNotFoundException("Principal must be of type Token");
	}

}
