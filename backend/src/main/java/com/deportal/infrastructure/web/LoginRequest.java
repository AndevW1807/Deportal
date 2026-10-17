package com.deportal.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Email @Size(max = 180) String email, @NotBlank String password) {
}
