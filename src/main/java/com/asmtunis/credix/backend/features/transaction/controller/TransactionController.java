package com.asmtunis.credix.backend.features.transaction.controller;

import com.asmtunis.credix.backend.common.dto.ResponseWrapper;
import com.asmtunis.credix.backend.features.transaction.dto.request.GenerateBarcodeRequest;
import com.asmtunis.credix.backend.features.transaction.dto.request.ProcessPaymentRequest;
import com.asmtunis.credix.backend.features.transaction.dto.response.BarcodeResponse;
import com.asmtunis.credix.backend.features.transaction.dto.response.TransactionResponse;
import com.asmtunis.credix.backend.features.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "APIs for transaction and payment processing")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping("/barcode/generate")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "Generate time-bound barcode", description = "Generate a 30-second expiry barcode for payment")
	public ResponseEntity<ResponseWrapper<BarcodeResponse>> generateBarcode(
			@Valid @RequestBody GenerateBarcodeRequest request,
			Authentication authentication
	) {
		try {
			String userEmail = authentication.getName();
			BarcodeResponse response = transactionService.generateBarcode(request, userEmail);
			return ResponseEntity.ok(new ResponseWrapper<>(
					org.springframework.http.HttpStatus.OK.value(),
					"Barcode generated successfully",
					response
			));
		} catch (RuntimeException e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
					.body(new ResponseWrapper<>(
							org.springframework.http.HttpStatus.BAD_REQUEST.value(),
							e.getMessage(),
							null
					));
		}
	}

	@PostMapping("/payment/process")
	@Operation(summary = "Process payment", description = "Process payment from POS terminal (ASM integration)")
	public ResponseEntity<ResponseWrapper<TransactionResponse>> processPayment(
			@Valid @RequestBody ProcessPaymentRequest request
	) {
		try {
			TransactionResponse response = transactionService.processPayment(request);
			return ResponseEntity.ok(new ResponseWrapper<>(
					org.springframework.http.HttpStatus.OK.value(),
					"Payment processed successfully",
					response
			));
		} catch (RuntimeException e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
					.body(new ResponseWrapper<>(
							org.springframework.http.HttpStatus.BAD_REQUEST.value(),
							e.getMessage(),
							null
					));
		}
	}

	@GetMapping("/{transactionId}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Get transaction by ID", description = "Retrieve transaction details by transaction ID")
	public ResponseEntity<ResponseWrapper<TransactionResponse>> getTransaction(
			@PathVariable String transactionId
	) {
		try {
			TransactionResponse response = transactionService.getTransactionById(transactionId);
			return ResponseEntity.ok(new ResponseWrapper<>(
					org.springframework.http.HttpStatus.OK.value(),
					"Transaction retrieved successfully",
					response
			));
		} catch (RuntimeException e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
					.body(new ResponseWrapper<>(
							org.springframework.http.HttpStatus.NOT_FOUND.value(),
							e.getMessage(),
							null
					));
		}
	}

	@GetMapping("/history")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "Get user transaction history", description = "Get all transactions for the authenticated user")
	public ResponseEntity<ResponseWrapper<List<TransactionResponse>>> getUserHistory(
			Authentication authentication
	) {
		try {
			String userEmail = authentication.getName();
			List<TransactionResponse> response = transactionService.getUserTransactionHistory(userEmail);
			return ResponseEntity.ok(new ResponseWrapper<>(
					org.springframework.http.HttpStatus.OK.value(),
					"Transaction history retrieved successfully",
					response
			));
		} catch (RuntimeException e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
					.body(new ResponseWrapper<>(
							org.springframework.http.HttpStatus.BAD_REQUEST.value(),
							e.getMessage(),
							null
					));
		}
	}

	@GetMapping("/admin/history")
	@PreAuthorize("hasAuthority('ADMIN')")
	@Operation(summary = "Get admin transaction history", description = "Get all transactions for users under the admin")
	public ResponseEntity<ResponseWrapper<List<TransactionResponse>>> getAdminHistory(
			Authentication authentication
	) {
		try {
			String adminEmail = authentication.getName();
			List<TransactionResponse> response = transactionService.getAdminTransactionHistory(adminEmail);
			return ResponseEntity.ok(new ResponseWrapper<>(
					org.springframework.http.HttpStatus.OK.value(),
					"Admin transaction history retrieved successfully",
					response
			));
		} catch (RuntimeException e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
					.body(new ResponseWrapper<>(
							org.springframework.http.HttpStatus.BAD_REQUEST.value(),
							e.getMessage(),
							null
					));
		}
	}

	@GetMapping("/date-range")
	@PreAuthorize("hasAuthority('ADMIN')")
	@Operation(summary = "Get transactions by date range", description = "Retrieve transactions within a specific date range")
	public ResponseEntity<ResponseWrapper<List<TransactionResponse>>> getTransactionsByDateRange(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
	) {
		try {
			List<TransactionResponse> response = transactionService.getTransactionsByDateRange(startDate, endDate);
			return ResponseEntity.ok(new ResponseWrapper<>(
					org.springframework.http.HttpStatus.OK.value(),
					"Transactions retrieved successfully",
					response
			));
		} catch (RuntimeException e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
					.body(new ResponseWrapper<>(
							org.springframework.http.HttpStatus.BAD_REQUEST.value(),
							e.getMessage(),
							null
					));
		}
	}

	@GetMapping("/stats/total-spent")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "Get total spent", description = "Get total amount spent by the user")
	public ResponseEntity<ResponseWrapper<Double>> getTotalSpent(Authentication authentication) {
		try {
			String userEmail = authentication.getName();
			Double totalSpent = transactionService.getTotalSpentByUser(userEmail);
			return ResponseEntity.ok(new ResponseWrapper<>(
					org.springframework.http.HttpStatus.OK.value(),
					"Total spent retrieved successfully",
					totalSpent
			));
		} catch (RuntimeException e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
					.body(new ResponseWrapper<>(
							org.springframework.http.HttpStatus.BAD_REQUEST.value(),
							e.getMessage(),
							null
					));
		}
	}
}
