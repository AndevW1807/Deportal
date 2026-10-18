package com.deportal.infrastructure.persistence.user;

import java.time.LocalDateTime;

import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 180)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 72)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private MembershipType type;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected UserJpaEntity() {
	}

	static UserJpaEntity newAccount(String name, String email, String passwordHash, UserRole role, MembershipType type) {
		UserJpaEntity account = new UserJpaEntity();
		account.name = name;
		account.email = email;
		account.passwordHash = passwordHash;
		account.role = role;
		account.type = type;
		return account;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public MembershipType getType() {
		return type;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

}
