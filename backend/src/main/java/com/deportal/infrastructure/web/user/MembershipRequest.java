package com.deportal.infrastructure.web.user;

import com.deportal.domain.user.MembershipType;

import jakarta.validation.constraints.NotNull;

public record MembershipRequest(@NotNull MembershipType membership) {
}
