package com.asmtunis.credix.backend.features.user.dto.request;

import com.asmtunis.credix.backend.features.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateUserRequest {
	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	private String email;

	@NotBlank(message = "Password is required")
	private String password;

	@NotNull(message = "Role is required")
	private Role role;

	private UUID adminId;

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

	public UUID getAdminId() {
		return adminId;
	}

	public void setAdminId(UUID adminId) {
		this.adminId = adminId;
	}
}
