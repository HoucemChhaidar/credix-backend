package com.asmtunis.credix.backend.features.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AddCreditToAdminRequest {
	@NotNull(message = "Tokenized ID is required")
	private String tokenizedId;

	@NotNull(message = "Amount is required")
	@Positive(message = "Amount must be positive")
	private Double amount;

	private String description;

	public String getTokenizedId() {
		return tokenizedId;
	}

	public void setTokenizedId(String tokenizedId) {
		this.tokenizedId = tokenizedId;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
