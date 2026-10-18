package com.deportal.application.auth.port;

public interface PasswordHasher {

	String hash(String rawPassword);

}
