package com.asmtunis.credix.backend.features.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class ProcessPaymentRequest {
	@NotBlank(message = "Barcode data is required")
	private String barcodeData;

	@NotNull(message = "Barcode expiry is required")
	private LocalDateTime barcodeExpiry;

	@NotNull(message = "Amount is required")
	@Positive(message = "Amount must be positive")
	private Double amount;

	@NotBlank(message = "Merchant ID is required")
	private String merchantId;

	@NotBlank(message = "Merchant name is required")
	private String merchantName;

	private String posTerminalId;

	public ProcessPaymentRequest() {}

	public String getBarcodeData() {
		return barcodeData;
	}

	public void setBarcodeData(String barcodeData) {
		this.barcodeData = barcodeData;
	}

	public LocalDateTime getBarcodeExpiry() {
		return barcodeExpiry;
	}

	public void setBarcodeExpiry(LocalDateTime barcodeExpiry) {
		this.barcodeExpiry = barcodeExpiry;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getPosTerminalId() {
		return posTerminalId;
	}

	public void setPosTerminalId(String posTerminalId) {
		this.posTerminalId = posTerminalId;
	}
}
