package com.asmtunis.credix.backend.features.user.dto.request;

import com.asmtunis.credix.backend.features.user.entity.Role;
import jakarta.validation.constraints.Email;

public class UpdateUserRequest {
	@Email(message = "Invalid email format")
	private String email;

	private String password;

	private Role role;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
