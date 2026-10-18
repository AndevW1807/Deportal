package com.deportal.application.auth.port;

public interface PasswordMatcher {

	boolean matches(String rawPassword, String passwordHash);

	String dummyHash();

}
