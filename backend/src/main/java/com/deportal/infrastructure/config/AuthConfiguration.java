package com.deportal.infrastructure.config;

import com.deportal.application.auth.login.Login;
import com.deportal.application.auth.port.PasswordHasher;
import com.deportal.application.auth.port.PasswordMatcher;
import com.deportal.application.auth.register.Register;
import com.deportal.application.auth.session.CurrentUser;
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

	@Bean
	Register register(UserRepository users, PasswordHasher passwords) {
		return new Register(users, passwords);
	}

}
