package com.deportal.application.auth;

import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRole;

public record AuthenticatedUser(Long id, String name, String email, UserRole role, MembershipType type) {
}
