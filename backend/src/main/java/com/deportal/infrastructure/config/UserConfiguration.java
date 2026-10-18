package com.deportal.infrastructure.config;

import com.deportal.application.user.ChangeMembership;
import com.deportal.application.user.ListCustomers;
import com.deportal.domain.user.UserRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {

	@Bean
	ListCustomers listCustomers(UserRepository users) {
		return new ListCustomers(users);
	}

	@Bean
	ChangeMembership changeMembership(UserRepository users) {
		return new ChangeMembership(users);
	}

}
