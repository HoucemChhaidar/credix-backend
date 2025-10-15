package com.asmtunis.credix.backend.features.store.service;

import com.asmtunis.credix.backend.features.store.dto.response.StoreResponse;
import com.asmtunis.credix.backend.features.store.entity.Store;
import com.asmtunis.credix.backend.features.store.entity.StoreType;
import com.asmtunis.credix.backend.features.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {
	private final StoreRepository storeRepository;

	public StoreService(StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
	}

	public List<StoreResponse> getAllStores() {
		return storeRepository.findByActiveTrue().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public List<StoreResponse> getStoresByType(StoreType storeType) {
		return storeRepository.findByStoreTypeAndActiveTrue(storeType).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public List<StoreResponse> getNearbyStores(Double latitude, Double longitude, Double radiusKm) {
		return storeRepository.findNearbyStores(latitude, longitude, radiusKm).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public StoreResponse getStoreById(Long id) {
		Store store = storeRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Store not found"));
		return mapToResponse(store);
	}

	private StoreResponse mapToResponse(Store store) {
		return new StoreResponse(
				store.getId(),
				store.getName(),
				store.getStoreType(),
				store.getMerchant().getName(),
				store.getDescription(),
				store.getAddress(),
				store.getLatitude(),
				store.getLongitude(),
				store.getImageUrl()
		);
	}
}
