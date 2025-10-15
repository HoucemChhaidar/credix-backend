package com.asmtunis.credix.backend.features.store.dto.response;

import com.asmtunis.credix.backend.features.store.entity.StoreType;

public record StoreResponse(
		Long id,
		String name,
		StoreType type,
		String merchantName,
		String description,
		String address,
		Double latitude,
		Double longitude,
		String imageUrl
) {}
