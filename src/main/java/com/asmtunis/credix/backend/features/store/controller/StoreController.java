package com.asmtunis.credix.backend.features.store.controller;

import com.asmtunis.credix.backend.common.dto.ResponseWrapper;
import com.asmtunis.credix.backend.features.store.dto.response.StoreResponse;
import com.asmtunis.credix.backend.features.store.entity.StoreType;
import com.asmtunis.credix.backend.features.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@Tag(name = "Store Management", description = "APIs for managing and discovering stores with ASM POS systems")
public class StoreController {
	private final StoreService storeService;

	public StoreController(StoreService storeService) {
		this.storeService = storeService;
	}

	@GetMapping
	@Operation(summary = "Get all stores", description = "Retrieve a list of all stores that have ASM POS systems")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Stores retrieved successfully")
	})
	public ResponseEntity<ResponseWrapper<List<StoreResponse>>> getAllStores() {
		List<StoreResponse> stores = storeService.getAllStores();
		return ResponseEntity.ok(new ResponseWrapper<>(
				200,
				"Stores retrieved successfully",
				stores
		));
	}

	@GetMapping("/type/{type}")
	@Operation(summary = "Get stores by type", description = "Retrieve stores filtered by their type (RETAIL, RESTAURANT, CAFE, CLOTHING, etc.)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Stores retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid store type")
	})
	public ResponseEntity<ResponseWrapper<List<StoreResponse>>> getStoresByType(
			@Parameter(description = "Type of store (RETAIL, RESTAURANT, CAFE, CLOTHING, GROCERY, PHARMACY, BOOKSTORE, GAS_STATION, ELECTRONICS, OTHER)", required = true)
			@PathVariable StoreType type
	) {
		List<StoreResponse> stores = storeService.getStoresByType(type);
		return ResponseEntity.ok(new ResponseWrapper<>(
				200,
				"Stores retrieved successfully",
				stores
		));
	}

	@GetMapping("/nearby")
	@Operation(summary = "Get nearby stores", description = "Find stores within a specified radius from given coordinates")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Nearby stores retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid coordinates or radius")
	})
	public ResponseEntity<ResponseWrapper<List<StoreResponse>>> getNearbyStores(
			@Parameter(description = "Latitude of user's current location", required = true, example = "36.8065")
			@RequestParam Double latitude,
			@Parameter(description = "Longitude of user's current location", required = true, example = "10.1815")
			@RequestParam Double longitude,
			@Parameter(description = "Search radius in kilometers", example = "5.0")
			@RequestParam(defaultValue = "5.0") Double radiusKm
	) {
		List<StoreResponse> stores = storeService.getNearbyStores(latitude, longitude, radiusKm);
		return ResponseEntity.ok(new ResponseWrapper<>(
				200,
				"Nearby stores retrieved successfully",
				stores
		));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get store by ID", description = "Retrieve detailed information about a specific store")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Store retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Store not found")
	})
	public ResponseEntity<ResponseWrapper<StoreResponse>> getStoreById(
			@Parameter(description = "Store ID", required = true, example = "1")
			@PathVariable Long id
	) {
		StoreResponse store = storeService.getStoreById(id);
		return ResponseEntity.ok(new ResponseWrapper<>(
				200,
				"Store retrieved successfully",
				store
		));
	}
}
