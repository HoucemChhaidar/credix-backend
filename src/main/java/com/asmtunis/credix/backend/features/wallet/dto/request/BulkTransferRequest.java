package com.asmtunis.credix.backend.features.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BulkTransferRequest {
	@NotNull(message = "Total amount is required")
	@Positive(message = "Total amount must be positive")
	private Double totalAmount;

	public BulkTransferRequest() {
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
}
