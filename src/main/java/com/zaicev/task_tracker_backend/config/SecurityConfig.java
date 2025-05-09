package com.zaicev.task_tracker_backend.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@ComponentScan("com.zaicev.task_tracker_backend.security")
public class SecurityConfig {

}
