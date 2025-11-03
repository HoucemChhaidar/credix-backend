package com.asmtunis.credix.backend.features.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public class CreateWalletRequest {
	@NotNull(message = "User ID is required")
	private UUID userId;

	@PositiveOrZero(message = "Transfer amount must be zero or positive")
	private Double transferAmount = 0.0;

	public CreateWalletRequest() {
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public Double getTransferAmount() {
		return transferAmount;
	}

	public void setTransferAmount(Double transferAmount) {
		this.transferAmount = transferAmount;
	}
}
