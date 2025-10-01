package com.asmtunis.credix.backend.features.transaction.dto.response;

import java.time.LocalDateTime;

public class BarcodeResponse {
	private String transactionId;
	private String barcodeData;
	private LocalDateTime expiresAt;
	private Double amount;
	private String description;
	private Integer expiresInSeconds;

	public BarcodeResponse() {
	}

	public BarcodeResponse(
			String transactionId, String barcodeData, LocalDateTime expiresAt,
			Double amount, String description, Integer expiresInSeconds
	) {
		this.transactionId = transactionId;
		this.barcodeData = barcodeData;
		this.expiresAt = expiresAt;
		this.amount = amount;
		this.description = description;
		this.expiresInSeconds = expiresInSeconds;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getBarcodeData() {
		return barcodeData;
	}

	public void setBarcodeData(String barcodeData) {
		this.barcodeData = barcodeData;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
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

	public Integer getExpiresInSeconds() {
		return expiresInSeconds;
	}

	public void setExpiresInSeconds(Integer expiresInSeconds) {
		this.expiresInSeconds = expiresInSeconds;
	}
}
