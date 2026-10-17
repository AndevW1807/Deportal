package com.deportal.infrastructure.web;

public record LoginResponse(String tokenType, String accessToken, long expiresIn) {
}
