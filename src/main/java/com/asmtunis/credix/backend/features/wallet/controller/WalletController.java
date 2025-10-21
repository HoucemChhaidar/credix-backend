package com.asmtunis.credix.backend.features.wallet.controller;

import com.asmtunis.credix.backend.common.dto.ResponseWrapper;
import com.asmtunis.credix.backend.features.wallet.dto.request.AddCreditRequest;
import com.asmtunis.credix.backend.features.wallet.dto.request.CreateWalletRequest;
import com.asmtunis.credix.backend.features.wallet.dto.response.WalletResponse;
import com.asmtunis.credix.backend.features.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/wallets")
@Tag(name = "Wallet Management", description = "Enterprise credit wallet management endpoints")
@SecurityRequirement(name = "Bearer Authentication") public class WalletController {
	private final WalletService walletService;

	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	/**
	 * Create a new wallet for a user (ADMIN only)
	 */
	@PostMapping @PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create wallet", description = "Create a new wallet for a user. Only admin users can create wallets for their employees.")
	@ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Wallet created successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "400", description = "Wallet already exists or validation error", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"), @ApiResponse(responseCode = "404", description = "User not found")})
	public ResponseEntity<ResponseWrapper<WalletResponse>> createWallet(
			@Valid @RequestBody @Parameter(description = "Wallet creation request containing user ID") CreateWalletRequest request,
			Authentication authentication
	) {
		try {
			String adminEmail = authentication.getName();
			WalletResponse walletResponse = walletService.createWallet(request, adminEmail);

			ResponseWrapper<WalletResponse> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), "Wallet created successfully", walletResponse);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (RuntimeException e) {
			ResponseWrapper<WalletResponse> errorResponse = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}

	/**
	 * Get wallet by user ID (ADMIN only)
	 */
	@GetMapping("/user/{userId}") @PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get wallet by user ID", description = "Retrieve wallet information for a specific user by their ID. Only admin users can access employee wallets.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Wallet retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"), @ApiResponse(responseCode = "404", description = "Wallet not found for the specified user")})
	public ResponseEntity<ResponseWrapper<WalletResponse>> getWalletByUserId(
			@PathVariable @Parameter(description = "User ID to retrieve wallet for", example = "123") UUID userId,
			Authentication authentication
	) {
		try {
			WalletResponse walletResponse = walletService.getWalletByUserId(userId);

			ResponseWrapper<WalletResponse> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Wallet retrieved successfully", walletResponse);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<WalletResponse> errorResponse = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}
	}

	/**
	 * Get current user's wallet (USER, ADMIN)
	 */
	@GetMapping("/my-wallet") @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Get my wallet", description = "Get current authenticated user's wallet information. Users can only access their own wallet.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Wallet retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "401", description = "Authentication required"), @ApiResponse(responseCode = "404", description = "Wallet not found for current user")})
	public ResponseEntity<ResponseWrapper<WalletResponse>> getMyWallet(Authentication authentication) {
		try {
			String userEmail = authentication.getName();
			WalletResponse walletResponse = walletService.getWalletByUserEmail(userEmail);

			ResponseWrapper<WalletResponse> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Wallet retrieved successfully", walletResponse);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<WalletResponse> errorResponse = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}
	}

	/**
	 * Get all wallets managed by admin (ADMIN only)
	 */
	@GetMapping("/admin") @PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get admin wallets", description = "Retrieve all wallets managed by the authenticated admin user.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Wallets retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")})
	public ResponseEntity<ResponseWrapper<List<WalletResponse>>> getWalletsByAdmin(
			Authentication authentication
	) {
		try {
			String adminEmail = authentication.getName();
			List<WalletResponse> wallets = walletService.getWalletsByAdmin(adminEmail);

			ResponseWrapper<List<WalletResponse>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Wallets retrieved successfully", wallets);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<List<WalletResponse>> errorResponse = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}

	/**
	 * Add credit to a user's wallet (ADMIN only)
	 */
	@PostMapping("/add-credit") @PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Add credit to wallet", description = "Add credit to a user's wallet. Only admin users can add credit to their employees' wallets.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Credit added successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "400", description = "Invalid request or validation error"), @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"), @ApiResponse(responseCode = "404", description = "Wallet not found")})
	public ResponseEntity<ResponseWrapper<WalletResponse>> addCredit(
			@Valid @RequestBody @Parameter(description = "Credit addition request containing user ID and amount") AddCreditRequest request,
			Authentication authentication
	) {
		try {
			String adminEmail = authentication.getName();
			WalletResponse walletResponse = walletService.addCredit(request, adminEmail);

			ResponseWrapper<WalletResponse> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Credit added successfully", walletResponse);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<WalletResponse> errorResponse = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}

	/**
	 * Get balance by user ID (ADMIN only)
	 */
	@GetMapping("/balance/{userId}") @PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get user balance", description = "Get wallet balance for a specific user by their ID. Only admin users can check employee balances.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Balance retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"), @ApiResponse(responseCode = "404", description = "Wallet not found")})
	public ResponseEntity<ResponseWrapper<Double>> getBalanceByUserId(
			@PathVariable @Parameter(description = "User ID to get balance for", example = "123") UUID userId
	) {
		try {
			Double balance = walletService.getBalanceByUserId(userId);

			ResponseWrapper<Double> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Balance retrieved successfully", balance);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<Double> errorResponse = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}
	}

	/**
	 * Get current user's balance (USER, ADMIN)
	 */
	@GetMapping("/my-balance") @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Get my balance", description = "Get current authenticated user's wallet balance.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Balance retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "401", description = "Authentication required"), @ApiResponse(responseCode = "404", description = "Wallet not found for current user")})
	public ResponseEntity<ResponseWrapper<Double>> getMyBalance(Authentication authentication) {
		try {
			String userEmail = authentication.getName();
			Double balance = walletService.getBalanceByUserEmail(userEmail);

			ResponseWrapper<Double> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Balance retrieved successfully", balance);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<Double> errorResponse = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}
	}

	/**
	 * Deduct credit from wallet (for payments) - ADMIN only
	 */
	@PostMapping("/deduct/{userId}") @PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Deduct credit", description = "Deduct credit from a user's wallet for payment processing. Available to admins only.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Credit deducted successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid amount"), @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"), @ApiResponse(responseCode = "404", description = "Wallet not found")})
	public ResponseEntity<ResponseWrapper<WalletResponse>> deductCredit(
			@PathVariable @Parameter(description = "User ID to deduct credit from", example = "123") UUID userId,
			@RequestParam @Parameter(description = "Amount to deduct", example = "25.50") Double amount,
			@RequestParam(required = false) @Parameter(description = "Optional description for the transaction", example = "Coffee purchase") String description
	) {
		try {
			WalletResponse walletResponse = walletService.deductCredit(userId, amount, description);

			ResponseWrapper<WalletResponse> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Credit deducted successfully", walletResponse);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<WalletResponse> errorResponse = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}

	/**
	 * Activate/Deactivate wallet (ADMIN only)
	 */
	@PutMapping("/toggle-status/{userId}") @PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Toggle wallet status", description = "Activate or deactivate a user's wallet. Only admin users can manage wallet status.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Wallet status updated successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "400", description = "Invalid request"), @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"), @ApiResponse(responseCode = "404", description = "Wallet not found")})
	public ResponseEntity<ResponseWrapper<WalletResponse>> toggleWalletStatus(
			@PathVariable @Parameter(description = "User ID to toggle wallet status for", example = "123") UUID userId,
			@RequestParam @Parameter(description = "New wallet status", example = "true") Boolean active,
			Authentication authentication
	) {
		try {
			String adminEmail = authentication.getName();
			WalletResponse walletResponse = walletService.toggleWalletStatus(userId, active, adminEmail);

			String message = active ? "Wallet activated successfully" : "Wallet deactivated successfully";
			ResponseWrapper<WalletResponse> response = new ResponseWrapper<>(HttpStatus.OK.value(), message, walletResponse);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<WalletResponse> errorResponse = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}

	/**
	 * Get wallet by tokenized ID (for barcode payments) - Public endpoint
	 */
	@GetMapping("/tokenized/{tokenizedId}")
	@Operation(summary = "Get wallet by barcode", description = "Retrieve wallet information using tokenized ID from barcode. This is a public endpoint for payment processing.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Wallet retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "404", description = "Wallet not found with the provided tokenized ID")})
	public ResponseEntity<ResponseWrapper<WalletResponse>> getWalletByTokenizedId(
			@PathVariable @Parameter(description = "Tokenized wallet ID from barcode", example = "WLT-123456-abcd1234") String tokenizedId
	) {
		try {
			WalletResponse walletResponse = walletService.getWalletByTokenizedId(tokenizedId);

			ResponseWrapper<WalletResponse> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Wallet retrieved successfully", walletResponse);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<WalletResponse> errorResponse = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}
	}

	/**
	 * Get all active wallets (SUPER_ADMIN only)
	 */
	@GetMapping("/super-admin/active") @PreAuthorize("hasRole('SUPER_ADMIN')")
	@Operation(summary = "Get all active wallets", description = "Retrieve all active wallets in the system. Only super administrators can access this endpoint.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Active wallets retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "403", description = "Access denied - Super Admin role required")})
	public ResponseEntity<ResponseWrapper<List<WalletResponse>>> getAllActiveWallets() {
		try {
			List<WalletResponse> wallets = walletService.getAllActiveWallets();

			ResponseWrapper<List<WalletResponse>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Active wallets retrieved successfully", wallets);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<List<WalletResponse>> errorResponse = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}

	/**
	 * Transfer credit between wallets (ADMIN only)
	 */
	@PostMapping("/transfer") @PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Transfer credit between wallets", description = "Transfer credit from one user's wallet to another within the same organization.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Credit transferred successfully", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))), @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid transfer"), @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"), @ApiResponse(responseCode = "404", description = "One or both wallets not found")})
	public ResponseEntity<ResponseWrapper<String>> transferCredit(
			@RequestParam @Parameter(description = "Source user ID to transfer from", example = "123") UUID fromUserId,
			@RequestParam @Parameter(description = "Destination user ID to transfer to", example = "456") UUID toUserId,
			@RequestParam @Parameter(description = "Amount to transfer", example = "50.00") Double amount,
			Authentication authentication
	) {
		try {
			String adminEmail = authentication.getName();
			walletService.transferCredit(fromUserId, toUserId, amount, adminEmail);

			ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Credit transferred successfully", "Transfer completed");
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			ResponseWrapper<String> errorResponse = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}
}
