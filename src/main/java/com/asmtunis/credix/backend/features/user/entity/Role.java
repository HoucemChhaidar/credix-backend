package com.asmtunis.credix.backend.features.user.entity;

public enum Role {
	USER,        // Mobile app user who works under an Admin (company employee)
	ADMIN,       // Company HR/Director who manages their users
	SUPER_ADMIN  // ASM (system owners) who manage all admins/companies
}
