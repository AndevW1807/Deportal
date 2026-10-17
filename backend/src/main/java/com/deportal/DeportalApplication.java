package com.deportal;

import com.deportal.infrastructure.config.EnvFileLoader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeportalApplication {

	public static void main(String[] args) {
		EnvFileLoader.load();
		SpringApplication.run(DeportalApplication.class, args);
	}

}
