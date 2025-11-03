package com.asmtunis.credix.backend.features.transaction.service;

import com.asmtunis.credix.backend.features.user.entity.User;
import com.asmtunis.credix.backend.features.user.repository.UserRepository;
import com.asmtunis.credix.backend.features.merchant.entity.Merchant;
import com.asmtunis.credix.backend.features.merchant.repository.MerchantRepository;
import com.asmtunis.credix.backend.features.transaction.dto.request.GenerateBarcodeRequest;
import com.asmtunis.credix.backend.features.transaction.dto.request.ProcessPaymentRequest;
import com.asmtunis.credix.backend.features.transaction.dto.response.BarcodeResponse;
import com.asmtunis.credix.backend.features.transaction.dto.response.TransactionNotification;
import com.asmtunis.credix.backend.features.transaction.dto.response.TransactionResponse;
import com.asmtunis.credix.backend.features.transaction.entity.Transaction;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionStatus;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionType;
import com.asmtunis.credix.backend.features.transaction.repository.TransactionRepository;
import com.asmtunis.credix.backend.features.wallet.entity.Wallet;
import com.asmtunis.credix.backend.features.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

	private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

	private final TransactionRepository transactionRepository;
	private final WalletRepository walletRepository;
	private final UserRepository userRepository;
	private final MerchantRepository merchantRepository;
	private final BarcodeService barcodeService;
	private final WebSocketNotificationService notificationService;

	public TransactionService(TransactionRepository transactionRepository,
														WalletRepository walletRepository,
														UserRepository userRepository,
														MerchantRepository merchantRepository,
														BarcodeService barcodeService,
														WebSocketNotificationService notificationService) {
		this.transactionRepository = transactionRepository;
		this.walletRepository = walletRepository;
		this.userRepository = userRepository;
		this.merchantRepository = merchantRepository;
		this.barcodeService = barcodeService;
		this.notificationService = notificationService;
	}

	/**
	 * Generate a time-bound barcode for payment (30-second expiry)
	 * Only generates barcode token - no amount needed from user
	 */
	public BarcodeResponse generateBarcode(GenerateBarcodeRequest request, String userEmail) {
		// Find user and wallet
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

		Wallet wallet = walletRepository.findByUserId(user.getId())
				.orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userEmail));

		// Check if wallet is active
		if (!wallet.getActive()) {
			throw new RuntimeException("Wallet is not active");
		}

		// Generate barcode with only wallet identifier
		String barcodeData = barcodeService.generateBarcode(wallet.getTokenizedId());
		LocalDateTime barcodeExpiry = barcodeService.getBarcodeExpiry();

		return new BarcodeResponse(
				barcodeData,
				barcodeExpiry,
				barcodeService.getExpirySeconds()
		);
	}

	/**
	 * Process payment from POS terminal (ASM integration)
	 * POS provides the amount - backend validates balance and processes payment
	 */
	public TransactionResponse processPayment(ProcessPaymentRequest request) {
		logger.info("========================================");
		logger.info("PAYMENT PROCESSING STARTED");
		logger.info("Barcode: {}", request.getBarcodeData());
		logger.info("Amount: {}", request.getAmount());
		logger.info("========================================");

		LocalDateTime barcodeExpiry = barcodeService.extractBarcodeExpiry(request.getBarcodeData());

		if (!barcodeService.isBarcodeValid(request.getBarcodeData(), barcodeExpiry)) {
			logger.error("✗ Barcode validation failed - expired or invalid");
			throw new RuntimeException("Barcode has expired or is invalid");
		}

		logger.info("✓ Barcode validated successfully");

		String walletTokenId = barcodeService.extractWalletTokenId(request.getBarcodeData());

		Wallet wallet = walletRepository.findByTokenizedId(walletTokenId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for barcode"));

		logger.info("✓ Wallet found - User: {}", wallet.getUser().getEmail());
		logger.info("Checking balance - Current: {}, Required: {}", wallet.getBalance(), request.getAmount());

		// Check sufficient balance (amount comes from POS)
		if (wallet.getBalance() < request.getAmount()) {
			logger.error("✗ Insufficient balance - Current: {}, Required: {}", wallet.getBalance(), request.getAmount());
			throw new RuntimeException("Insufficient balance");
		}

		logger.info("✓ Balance check passed");

		Merchant merchant = null;
		String merchantName = "Unknown Merchant";
		if (request.getMerchantId() != null && !request.getMerchantId().isEmpty()) {
			merchant = merchantRepository.findByMerchantId(request.getMerchantId()).orElse(null);
			if (merchant != null) {
				merchantName = merchant.getName();
				logger.info("✓ Merchant found: {}", merchantName);
			}
		}

		Transaction transaction = new Transaction();
		transaction.setTransactionId(generateTransactionId());
		transaction.setUser(wallet.getUser());
		transaction.setWallet(wallet);
		transaction.setMerchant(merchant);
		transaction.setAmount(request.getAmount());
		transaction.setStatus(TransactionStatus.COMPLETED);
		transaction.setType(TransactionType.PAYMENT);
		transaction.setDescription("Payment via ASM POS");
		transaction.setBalanceBefore(wallet.getBalance());
		transaction.setBarcodeData(request.getBarcodeData());
		transaction.setPosTerminalId(request.getPosTerminalId());
		transaction.setCompletedAt(LocalDateTime.now());

		// Deduct amount from wallet
		wallet.setBalance(wallet.getBalance() - request.getAmount());
		walletRepository.save(wallet);

		logger.info("✓ Wallet balance updated - New balance: {}", wallet.getBalance());

		transaction.setBalanceAfter(wallet.getBalance());

		Transaction completedTransaction = transactionRepository.save(transaction);

		logger.info("✓ Transaction saved - ID: {}", completedTransaction.getTransactionId());

		TransactionNotification notification = new TransactionNotification(
				completedTransaction.getTransactionId(),
				completedTransaction.getCompletedAt(),
				merchantName,
				completedTransaction.getAmount()
		);

		String userEmail = wallet.getUser().getEmail();
		logger.info("Attempting to send WebSocket notification to: {}", userEmail);

		try {
			notificationService.sendTransactionNotification(userEmail, notification);
			logger.info("✓ Notification service called successfully");
		} catch (Exception e) {
			logger.error("✗ Failed to send notification: {}", e.getMessage(), e);
		}

		logger.info("========================================");
		logger.info("PAYMENT PROCESSING COMPLETED");
		logger.info("========================================");

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
	 * Get transaction history for all users under an admin
	 */
	@Transactional(readOnly = true)
	public List<TransactionResponse> getAdminTransactionHistory(String adminEmail) {
		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		List<Transaction> transactions = transactionRepository.findByAdminIdOrderByCreatedAtDesc(admin.getId());
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

	@Transactional(readOnly = true)
	public List<TransactionResponse> getCreditTransferHistory(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found: " + email));

		List<Transaction> transactions = transactionRepository.findCreditTransactionsByAdminId(user.getId());
		return transactions.stream()
				.map(TransactionResponse::new)
				.collect(Collectors.toList());
	}
}
