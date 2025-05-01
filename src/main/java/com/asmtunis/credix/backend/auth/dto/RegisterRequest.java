package com.asmtunis.credix.backend.auth.dto;

import com.asmtunis.credix.backend.auth.model.Role;

public class RegisterRequest {
	public String username;
	public String password;
	public Role role;
}
