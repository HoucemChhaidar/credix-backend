package com.asmtunis.credix.backend.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class CreateWalletRequest {
	@NotNull(message = "User ID is required")
	@Positive(message = "User ID must be positive")
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