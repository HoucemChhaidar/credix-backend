package com.asmtunis.credix.backend.features.transaction.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

@Service
public class BarcodeService {

	private static final int BARCODE_EXPIRY_SECONDS = 30;
	private static final String BARCODE_PREFIX = "ASM-PAY";
	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * Generate a time-bound barcode with Code128 format
	 * Format: ASM-PAY|{walletTokenId}|{timestamp}|{randomToken}
	 */
	public String generateBarcode(String walletTokenId) {
		long timestamp = System.currentTimeMillis();
		String randomToken = generateRandomToken();

		String barcodeData = String.format("%s|%s|%d|%s",
				BARCODE_PREFIX,
				walletTokenId,
				timestamp,
				randomToken
		);

		return Base64.getEncoder().encodeToString(barcodeData.getBytes());
	}

	/**
	 * Validate barcode format and expiry
	 */
	public boolean isBarcodeValid(String barcodeData, LocalDateTime barcodeExpiry) {
		if (barcodeData == null || barcodeExpiry == null) {
			return false;
		}

		if (LocalDateTime.now().isAfter(barcodeExpiry)) {
			return false;
		}

		try {

			String decoded = new String(Base64.getDecoder().decode(barcodeData));
			String[] parts = decoded.split("\\|");

			if (parts.length != 4) {
				return false;
			}

			return BARCODE_PREFIX.equals(parts[0]);
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
				return parts[1];
			}
		} catch (Exception e) {
			throw new RuntimeException("Invalid barcode format");
		}

		throw new RuntimeException("Could not extract wallet ID from barcode");
	}

	/**
	 * Extract barcode expiry time from the barcode data
	 * Reads the timestamp from barcode and adds 30 seconds
	 */
	public LocalDateTime extractBarcodeExpiry(String barcodeData) {
		try {
			String decoded = new String(Base64.getDecoder().decode(barcodeData));
			String[] parts = decoded.split("\\|");

			if (parts.length >= 3) {
				long timestamp = Long.parseLong(parts[2]);
				LocalDateTime generatedAt = LocalDateTime.ofInstant(
						Instant.ofEpochMilli(timestamp),
						ZoneId.systemDefault()
				);
				return generatedAt.plusSeconds(BARCODE_EXPIRY_SECONDS);
			}
		} catch (Exception e) {
			throw new RuntimeException("Invalid barcode format: " + e.getMessage());
		}

		throw new RuntimeException("Could not extract expiry from barcode");
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
