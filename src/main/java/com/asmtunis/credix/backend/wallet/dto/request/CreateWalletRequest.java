package com.asmtunis.credix.backend.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateWalletRequest {
	@NotNull(message = "User ID is required")
	@Positive(message = "User ID must be positive")
	private Long userId;

	public CreateWalletRequest() {
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}