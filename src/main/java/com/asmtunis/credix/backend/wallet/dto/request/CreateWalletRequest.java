package com.asmtunis.credix.backend.wallet.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateWalletRequest {
	@NotNull(message = "User ID is required")
	private UUID userId;

	public CreateWalletRequest() {
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}
}