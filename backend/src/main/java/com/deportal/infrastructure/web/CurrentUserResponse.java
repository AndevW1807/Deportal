package com.deportal.infrastructure.web;

public record CurrentUserResponse(Long id, String name, String email, String role, String type) {
}
