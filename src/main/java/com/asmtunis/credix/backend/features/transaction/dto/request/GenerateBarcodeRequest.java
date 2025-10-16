package com.asmtunis.credix.backend.features.transaction.dto.request;

public class GenerateBarcodeRequest {
	// Request body can be empty or contain optional metadata
	private String description;

	public GenerateBarcodeRequest() {}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
