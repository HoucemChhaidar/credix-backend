package com.asmtunis.credix.backend.features.transaction.dto.request;

public class GenerateBarcodeRequest {
	private String description;

	public GenerateBarcodeRequest() {}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
