package com.asmtunis.credix.backend.features.transaction.service;

import com.asmtunis.credix.backend.features.auth.entity.User;
import com.asmtunis.credix.backend.features.auth.repository.UserRepository;
import com.asmtunis.credix.backend.features.transaction.dto.request.GenerateBarcodeRequest;
import com.asmtunis.credix.backend.features.transaction.dto.request.ProcessPaymentRequest;
import com.asmtunis.credix.backend.features.transaction.dto.response.BarcodeResponse;
import com.asmtunis.credix.backend.features.transaction.dto.response.TransactionResponse;
import com.asmtunis.credix.backend.features.transaction.entity.Transaction;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionStatus;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionType;
import com.asmtunis.credix.backend.features.transaction.repository.TransactionRepository;
import com.asmtunis.credix.backend.features.wallet.entity.Wallet;
import com.asmtunis.credix.backend.features.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final WalletRepository walletRepository;
	private final UserRepository userRepository;
	private final BarcodeService barcodeService;

	public TransactionService(
			TransactionRepository transactionRepository,
			WalletRepository walletRepository,
			UserRepository userRepository,
			BarcodeService barcodeService
	) {
		this.transactionRepository = transactionRepository;
		this.walletRepository = walletRepository;
		this.userRepository = userRepository;
		this.barcodeService = barcodeService;
	}

	/**
	 * Generate a time-bound barcode for payment (30-second expiry)
	 * Removed transaction creation - only generates barcode token for Flutter to display
	 */
	public BarcodeResponse generateBarcode(GenerateBarcodeRequest request, String userEmail) {
		// Find user and wallet
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

		Wallet wallet = walletRepository.findByUserId(user.getId())
				.orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userEmail));

		// Check if wallet is active
		if (!wallet.getIsActive()) {
			throw new RuntimeException("Wallet is not active");
		}

		// Check sufficient balance
		if (wallet.getBalance() < request.getAmount()) {
			throw new RuntimeException("Insufficient balance. Current balance: " + wallet.getBalance());
		}

		String barcodeData = barcodeService.generateBarcode(
				wallet.getTokenizedId(),
				request.getAmount(),
				null // No transaction ID needed at this stage
		);
		LocalDateTime barcodeExpiry = barcodeService.getBarcodeExpiry();

		return new BarcodeResponse(
				null, // No transaction ID yet
				barcodeData,
				barcodeExpiry,
				request.getAmount(),
				request.getDescription(),
				barcodeService.getExpirySeconds()
		);
	}

	/**
	 * Process payment from POS terminal (ASM integration)
	 * Now creates the transaction when payment is actually processed
	 */
	public TransactionResponse processPayment(ProcessPaymentRequest request) {
		if (!barcodeService.isBarcodeValid(request.getBarcodeData(), request.getBarcodeExpiry())) {
			throw new RuntimeException("Barcode has expired or is invalid");
		}

		String walletTokenId = barcodeService.extractWalletTokenId(request.getBarcodeData());
		Double barcodeAmount = barcodeService.extractAmount(request.getBarcodeData());

		Wallet wallet = walletRepository.findByTokenizedId(walletTokenId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for barcode"));

		// Validate amount matches
		if (!barcodeAmount.equals(request.getAmount())) {
			throw new RuntimeException("Payment amount does not match barcode amount");
		}

		// Check sufficient balance
		if (wallet.getBalance() < request.getAmount()) {
			throw new RuntimeException("Insufficient balance");
		}

		Transaction transaction = new Transaction();
		transaction.setTransactionId(generateTransactionId());
		transaction.setUser(wallet.getUser());
		transaction.setWallet(wallet);
		transaction.setAmount(request.getAmount());
		transaction.setStatus(TransactionStatus.COMPLETED);
		transaction.setType(TransactionType.PAYMENT);
		transaction.setDescription("Payment via ASM POS");
		transaction.setBalanceBefore(wallet.getBalance());
		transaction.setBarcodeData(request.getBarcodeData());
		transaction.setMerchantId(request.getMerchantId());
		transaction.setMerchantName(request.getMerchantName());
		transaction.setPosTerminalId(request.getPosTerminalId());
		transaction.setCompletedAt(LocalDateTime.now());

		// Deduct amount from wallet
		wallet.setBalance(wallet.getBalance() - request.getAmount());
		walletRepository.save(wallet);

		transaction.setBalanceAfter(wallet.getBalance());

		Transaction completedTransaction = transactionRepository.save(transaction);

		return new TransactionResponse(completedTransaction);
	}

	/**
	 * Get transaction by ID
	 */
	@Transactional(readOnly = true)
	public TransactionResponse getTransactionById(String transactionId) {
		Transaction transaction = transactionRepository.findByTransactionId(transactionId)
				.orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
		return new TransactionResponse(transaction);
	}

	/**
	 * Get transaction history for a user
	 */
	@Transactional(readOnly = true)
	public List<TransactionResponse> getUserTransactionHistory(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

		List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
		return transactions.stream()
				.map(TransactionResponse::new)
				.collect(Collectors.toList());
	}

	/**
	 * Get transaction history for all users under a corporate
	 */
	@Transactional(readOnly = true)
	public List<TransactionResponse> getCorporateTransactionHistory(String corporateEmail) {
		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		List<Transaction> transactions = transactionRepository.findByCorporateIdOrderByCreatedAtDesc(corporate.getId());
		return transactions.stream()
				.map(TransactionResponse::new)
				.collect(Collectors.toList());
	}

	/**
	 * Get transactions by date range
	 */
	@Transactional(readOnly = true)
	public List<TransactionResponse> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Transaction> transactions = transactionRepository.findByDateRange(startDate, endDate);
		return transactions.stream()
				.map(TransactionResponse::new)
				.collect(Collectors.toList());
	}

	/**
	 * Get user spending statistics
	 */
	@Transactional(readOnly = true)
	public Double getTotalSpentByUser(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

		Double totalSpent = transactionRepository.getTotalSpentByUser(user.getId());
		return totalSpent != null ? totalSpent : 0.0;
	}

	/**
	 * Generate unique transaction ID
	 */
	private String generateTransactionId() {
		return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}
