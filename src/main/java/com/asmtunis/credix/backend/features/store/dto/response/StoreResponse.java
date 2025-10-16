package com.asmtunis.credix.backend.features.store.dto.response;

import com.asmtunis.credix.backend.features.store.entity.StoreType;

public record StoreResponse(
		Long id,
		String name,
		StoreType storeType,
		String merchantName,
		String address,
		Double latitude,
		Double longitude,
		String imageUrl
) {}
