package com.asmtunis.credix.backend.features.user.dto.response;

import com.asmtunis.credix.backend.features.user.entity.Role;
import com.asmtunis.credix.backend.features.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserListResponse {
	private UUID id;
	private String email;
	private Role role;
	private Boolean active;
	private UUID adminId;
	private String adminEmail;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public UserListResponse(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.role = user.getRole();
		this.active = user.getActive();
		this.createdAt = user.getCreatedAt();
		this.updatedAt = user.getUpdatedAt();

		if (user.getAdmin() != null) {
			this.adminId = user.getAdmin().getId();
			this.adminEmail = user.getAdmin().getEmail();
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public UUID getAdminId() {
		return adminId;
	}

	public void setAdminId(UUID adminId) {
		this.adminId = adminId;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
