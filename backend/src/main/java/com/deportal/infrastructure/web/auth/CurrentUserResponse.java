package com.deportal.infrastructure.web.auth;

public record CurrentUserResponse(Long id, String name, String email, String role, String type) {
}
