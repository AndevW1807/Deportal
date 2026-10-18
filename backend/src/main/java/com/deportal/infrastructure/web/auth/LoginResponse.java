package com.deportal.infrastructure.web.auth;

public record LoginResponse(String tokenType, String accessToken, long expiresIn) {
}
