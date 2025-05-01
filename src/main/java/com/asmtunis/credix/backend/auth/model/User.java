package com.asmtunis.credix.backend.auth.model;

import jakarta.persistence.*;

@Entity(name = "`user`") // Escape default 'User' model name in H2
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String username;
	private String password;
	@Enumerated(EnumType.STRING)
	private Role role;

	public String getUsername() {return username;}

	public void setUsername(String username) {this.username = username;}

	public String getPassword() {return password;}

	public void setPassword(String password) {this.password = password;}

	public Role getRole() {return role;}

	public void setRole(Role role) {this.role = role;}
}
