package com.asmtunis.credix.backend.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@ManyToOne
	@JoinColumn(name = "corporate_id")
	private User corporate;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public User() {}

	public User getCorporate() {
		return corporate;
	}

	public void setCorporate(User corporate) {
		this.corporate = corporate;
	}

	public Long getId() {return id;}

	public void setId(Long id) {this.id = id;}

	public String getEmail() {return email;}

	public void setEmail(String email) {this.email = email;}

	public String getPassword() {return password;}

	public void setPassword(String password) {this.password = password;}

	public Role getRole() {return role;}

	public void setRole(Role role) {this.role = role;}

	public LocalDateTime getCreatedAt() {return createdAt;}

	public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

	public LocalDateTime getUpdatedAt() {return updatedAt;}

	public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}