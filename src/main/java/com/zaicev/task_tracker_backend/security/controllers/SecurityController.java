package com.zaicev.task_tracker_backend.security.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class SecurityController {
	@GetMapping("/sign-in")
	public String signIn() {
		return "/auth/sign-in";
	}
}
