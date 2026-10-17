package com.deportal.infrastructure.config;

import com.deportal.application.auth.CurrentUser;
import com.deportal.application.auth.Login;
import com.deportal.application.auth.PasswordMatcher;
import com.deportal.domain.user.UserRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

	@Bean
	Login login(UserRepository users, PasswordMatcher passwords) {
		return new Login(users, passwords);
	}

	@Bean
	CurrentUser currentUser(UserRepository users) {
		return new CurrentUser(users);
	}

}
