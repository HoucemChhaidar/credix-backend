package com.asmtunis.credix.backend.features.transaction.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class BarcodeService {

	private static final int BARCODE_EXPIRY_SECONDS = 30;
	private static final String BARCODE_PREFIX = "ASM-PAY";
	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * Generate a time-bound barcode with Code128 format
	 * Format: ASM-PAY|{walletTokenId}|{amount}|{timestamp}|{randomToken}
	 */
	public String generateBarcode(String walletTokenId, Double amount, String transactionId) {
		long timestamp = System.currentTimeMillis();
		String randomToken = generateRandomToken();

		// Format: ASM-PAY|walletId|amount|timestamp|randomToken
		String barcodeData = String.format("%s|%s|%.3f|%d|%s",
				BARCODE_PREFIX,
				walletTokenId,
				amount,
				timestamp,
				randomToken
		);

		// Encode to Base64 for Code128 compatibility
		return Base64.getEncoder().encodeToString(barcodeData.getBytes());
	}

	/**
	 * Validate barcode format and expiry
	 */
	public boolean isBarcodeValid(String barcodeData, LocalDateTime barcodeExpiry) {
		if (barcodeData == null || barcodeExpiry == null) {
			return false;
		}

		// Check if barcode has expired
		if (LocalDateTime.now().isAfter(barcodeExpiry)) {
			return false;
		}

		try {
			// Decode and validate format
			String decoded = new String(Base64.getDecoder().decode(barcodeData));
			String[] parts = decoded.split("\\|");

			// Must have 5 parts: prefix|walletId|amount|timestamp|randomToken
			if (parts.length != 5) {
				return false;
			}

			// Validate prefix
			if (!BARCODE_PREFIX.equals(parts[0])) {
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Extract wallet token ID from barcode
	 */
	public String extractWalletTokenId(String barcodeData) {
		try {
			String decoded = new String(Base64.getDecoder().decode(barcodeData));
			String[] parts = decoded.split("\\|");

			if (parts.length >= 2) {
				return parts[1]; // Wallet token ID is the second part
			}
		} catch (Exception e) {
			throw new RuntimeException("Invalid barcode format");
		}

		throw new RuntimeException("Could not extract wallet ID from barcode");
	}

	/**
	 * Extract amount from barcode
	 */
	public Double extractAmount(String barcodeData) {
		try {
			String decoded = new String(Base64.getDecoder().decode(barcodeData));
			String[] parts = decoded.split("\\|");

			if (parts.length >= 3) {
				return Double.parseDouble(parts[2]); // Amount is the third part
			}
		} catch (Exception e) {
			throw new RuntimeException("Invalid barcode format");
		}

		throw new RuntimeException("Could not extract amount from barcode");
	}

	/**
	 * Get barcode expiry time (30 seconds from now)
	 */
	public LocalDateTime getBarcodeExpiry() {
		return LocalDateTime.now().plusSeconds(BARCODE_EXPIRY_SECONDS);
	}

	/**
	 * Get expiry duration in seconds
	 */
	public int getExpirySeconds() {
		return BARCODE_EXPIRY_SECONDS;
	}

	/**
	 * Generate a secure random token for barcode uniqueness
	 */
	private String generateRandomToken() {
		byte[] randomBytes = new byte[8];
		secureRandom.nextBytes(randomBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}

	/**
	 * Decode barcode for debugging/logging purposes
	 */
	public String decodeBarcode(String barcodeData) {
		try {
			return new String(Base64.getDecoder().decode(barcodeData));
		} catch (Exception e) {
			return "Invalid barcode";
		}
	}
}
