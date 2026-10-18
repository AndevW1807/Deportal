package com.deportal.domain.user;

public final class UserAccount {

	private final Long id;
	private final String name;
	private final String email;
	private final String passwordHash;
	private final UserRole role;
	private final MembershipType type;
	private final boolean deleted;

	public UserAccount(Long id, String name, String email, String passwordHash, UserRole role, MembershipType type,
			boolean deleted) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.type = type;
		this.deleted = deleted;
	}

	public Long id() {
		return id;
	}

	public String name() {
		return name;
	}

	public String email() {
		return email;
	}

	public String passwordHash() {
		return passwordHash;
	}

	public UserRole role() {
		return role;
	}

	public MembershipType type() {
		return type;
	}

	public boolean deleted() {
		return deleted;
	}

	public UserAccount withMembership(MembershipType type) {
		return new UserAccount(id, name, email, passwordHash, role, type, deleted);
	}

}
