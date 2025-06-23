package com.asmtunis.credix.backend.features.auth.dto.response;

import com.asmtunis.credix.backend.features.auth.entity.Role;
import com.asmtunis.credix.backend.features.auth.entity.User;

public class UserResponse {
	private String email;
	private Role role;

	public UserResponse() {
	}

	public UserResponse(User user) {
		this.email = user.getEmail();
		this.role = user.getRole();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}