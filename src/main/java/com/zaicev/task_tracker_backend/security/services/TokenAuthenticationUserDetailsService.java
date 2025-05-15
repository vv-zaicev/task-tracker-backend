package com.zaicev.task_tracker_backend.security.services;

import java.util.HashSet;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.zaicev.task_tracker_backend.security.models.Token;
import com.zaicev.task_tracker_backend.security.models.UserDetailsImpl;

public class TokenAuthenticationUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken>{

	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken) throws UsernameNotFoundException {
		if(authenticationToken.getPrincipal() instanceof Token token) {
			return new UserDetailsImpl(token.subject(), "nopassword", new HashSet<String>(token.authorites()));
		}
		
		
		throw new UsernameNotFoundException("Principal must be of type Token");
	}
	
}
