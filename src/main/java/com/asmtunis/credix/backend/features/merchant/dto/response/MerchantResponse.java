package com.asmtunis.credix.backend.features.merchant.dto.response;

public record MerchantResponse(
		Long id,
		String merchantId,
		String name,
		String email,
		String phoneNumber
) {}
