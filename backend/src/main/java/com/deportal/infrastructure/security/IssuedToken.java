package com.deportal.infrastructure.security;

public record IssuedToken(String value, long expiresInSeconds) {
}
