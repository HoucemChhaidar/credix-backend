package com.asmtunis.credix.backend.auth.model;

import jakarta.persistence.*;

@Entity(name = "`user`") // Escape default 'User' model name in H2
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String email;
	private String password;
	@Enumerated(EnumType.STRING)
	private Role role;

	public String getEmail() {return email;}

	public void setEmail(String email) {this.email = email;}

	public String getPassword() {return password;}

	public void setPassword(String password) {this.password = password;}

	public Role getRole() {return role;}

	public void setRole(Role role) {this.role = role;}
}
