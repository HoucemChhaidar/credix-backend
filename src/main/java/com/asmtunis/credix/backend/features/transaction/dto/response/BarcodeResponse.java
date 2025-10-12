package com.asmtunis.credix.backend.features.transaction.dto.response;

import java.time.LocalDateTime;

public class BarcodeResponse {
	private String barcodeData;
	private LocalDateTime expiresAt;
	private Integer expiresInSeconds;

	public BarcodeResponse() {}

	public BarcodeResponse(String barcodeData, LocalDateTime expiresAt, Integer expiresInSeconds) {
		this.barcodeData = barcodeData;
		this.expiresAt = expiresAt;
		this.expiresInSeconds = expiresInSeconds;
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

	public Integer getExpiresInSeconds() {
		return expiresInSeconds;
	}

	public void setExpiresInSeconds(Integer expiresInSeconds) {
		this.expiresInSeconds = expiresInSeconds;
	}
}
