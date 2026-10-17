package com.deportal.application.auth;

public interface PasswordMatcher {

	boolean matches(String rawPassword, String passwordHash);

	String dummyHash();

}
