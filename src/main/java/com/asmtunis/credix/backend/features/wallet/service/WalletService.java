package com.asmtunis.credix.backend.features.wallet.service;

import com.asmtunis.credix.backend.features.transaction.entity.Transaction;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionStatus;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionType;
import com.asmtunis.credix.backend.features.transaction.repository.TransactionRepository;
import com.asmtunis.credix.backend.features.user.entity.User;
import com.asmtunis.credix.backend.features.user.repository.UserRepository;
import com.asmtunis.credix.backend.features.wallet.dto.request.AddCreditRequest;
import com.asmtunis.credix.backend.features.wallet.dto.request.AddCreditToAdminRequest;
import com.asmtunis.credix.backend.features.wallet.dto.request.CreateWalletRequest;
import com.asmtunis.credix.backend.features.wallet.dto.response.BulkTransferResponse;
import com.asmtunis.credix.backend.features.wallet.dto.response.WalletResponse;
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
public class WalletService {
	private final WalletRepository walletRepository;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;

	public WalletService(
			WalletRepository walletRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
		this.walletRepository = walletRepository;
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
	}

	/**
	 * Create a new wallet for a user (ID-based)
	 */
	public WalletResponse createWallet(CreateWalletRequest request, String adminEmail) {
		// Check if wallet already exists for this user
		if (walletRepository.existsByUserId(request.getUserId())) {
			throw new RuntimeException("Wallet already exists for user ID: " + request.getUserId());
		}

		// Find the user by ID
		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		if (user.getAdmin() == null || !user.getAdmin().getId().equals(admin.getId())) {
			throw new RuntimeException("Unauthorized: User doesn't belong to this admin");
		}

		// Create and save the wallet
		Wallet wallet = new Wallet();
		wallet.setUser(user);
		wallet.setTokenizedId(generateTokenizedId(user.getEmail()));
		wallet.setTransferAmount(request.getTransferAmount());

		Wallet savedWallet = walletRepository.save(wallet);
		return new WalletResponse(savedWallet);
	}

	/**
	 * Get wallet by user ID
	 */
	@Transactional(readOnly = true)
	public WalletResponse getWalletByUserId(UUID userId) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));
		return new WalletResponse(wallet);
	}

	/**
	 * Get wallet by user email (for JWT authentication scenarios)
	 */
	@Transactional(readOnly = true)
	public WalletResponse getWalletByUserEmail(String userEmail) {
		Wallet wallet = walletRepository.findByUserEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userEmail));
		return new WalletResponse(wallet);
	}

	/**
	 * Get balance by user ID
	 */
	@Transactional(readOnly = true)
	public Double getBalanceByUserId(UUID userId) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));
		return wallet.getBalance();
	}

	/**
	 * Get balance by user email
	 */
	@Transactional(readOnly = true)
	public Double getBalanceByUserEmail(String userEmail) {
		Wallet wallet = walletRepository.findByUserEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userEmail));
		return wallet.getBalance();
	}

	/**
	 * Get all wallets managed by an admin (using admin email from JWT)
	 */
	@Transactional(readOnly = true)
	public List<WalletResponse> getWalletsByAdmin(String adminEmail) {
		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		List<Wallet> wallets = walletRepository.findByUserAdminId(admin.getId());
		return wallets.stream()
				.map(WalletResponse::new)
				.collect(Collectors.toList());
	}

	/**
	 * Add credit to a user's wallet (ID-based)
	 */
	public WalletResponse addCredit(AddCreditRequest request, String adminEmail) {
		// Find wallet by user ID
		Wallet wallet = walletRepository.findByUserId(request.getUserId())
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + request.getUserId()));

		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		if (wallet.getUser().getAdmin() == null ||
				!wallet.getUser().getAdmin().getId().equals(admin.getId())) {
			throw new RuntimeException("Unauthorized: Admin doesn't manage this user");
		}

		Double balanceBefore = wallet.getBalance();

		// Add credit to wallet
		wallet.setBalance(wallet.getBalance() + request.getAmount());

		Wallet savedWallet = walletRepository.save(wallet);

		createCreditTransaction(wallet, request.getAmount(), balanceBefore, "Credit added by admin");

		return new WalletResponse(savedWallet);
	}

	/**
	 * Add credit to an admin's wallet (SUPER_ADMIN only)
	 */
	public WalletResponse addCreditToAdmin(AddCreditToAdminRequest request, String superAdminEmail) {
		Wallet wallet = walletRepository.findByTokenizedId(request.getTokenizedId())
				.orElseThrow(() -> new RuntimeException("Wallet not found with tokenized ID: " + request.getTokenizedId()));

		User superAdmin = userRepository.findByEmail(superAdminEmail)
				.orElseThrow(() -> new RuntimeException("Super Admin not found: " + superAdminEmail));

		// Verify the wallet belongs to an ADMIN managed by this SUPER_ADMIN
		if (wallet.getUser().getAdmin() == null ||
				!wallet.getUser().getAdmin().getId().equals(superAdmin.getId())) {
			throw new RuntimeException("Unauthorized: This admin doesn't belong to you");
		}

		// Verify the user is actually an ADMIN
		if (wallet.getUser().getRole() != com.asmtunis.credix.backend.features.user.entity.Role.ADMIN) {
			throw new RuntimeException("Target user is not an admin");
		}

		Double balanceBefore = wallet.getBalance();

		// Add credit to admin wallet
		wallet.setBalance(wallet.getBalance() + request.getAmount());

		Wallet savedWallet = walletRepository.save(wallet);

		String description = request.getDescription() != null ? request.getDescription() : "Credit added by super admin";
		createCreditTransaction(wallet, request.getAmount(), balanceBefore, description);

		return new WalletResponse(savedWallet);
	}

	/**
	 * Deduct credit from wallet (for payments)
	 */
	public WalletResponse deductCredit(UUID userId, Double amount, String description) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

		if (wallet.getBalance() < amount) {
			throw new RuntimeException("Insufficient balance. Current balance: " + wallet.getBalance());
		}

		wallet.setBalance(wallet.getBalance() - amount);
		Wallet savedWallet = walletRepository.save(wallet);
		return new WalletResponse(savedWallet);
	}

	/**
	 * Activate/Deactivate wallet
	 */
	public WalletResponse toggleWalletStatus(UUID userId, Boolean active, String adminEmail) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		if (wallet.getUser().getAdmin() == null ||
				!wallet.getUser().getAdmin().getId().equals(admin.getId())) {
			throw new RuntimeException("Unauthorized: Admin doesn't manage this user");
		}

		wallet.setActive(active);
		Wallet savedWallet = walletRepository.save(wallet);
		return new WalletResponse(savedWallet);
	}

	/**
	 * Get wallet by tokenized ID (for barcode payments)
	 */
	@Transactional(readOnly = true)
	public WalletResponse getWalletByTokenizedId(String tokenizedId) {
		Wallet wallet = walletRepository.findByTokenizedId(tokenizedId)
				.orElseThrow(() -> new RuntimeException("Wallet not found with tokenized ID: " + tokenizedId));
		return new WalletResponse(wallet);
	}

	/**
	 * Get all active wallets
	 */
	@Transactional(readOnly = true)
	public List<WalletResponse> getAllActiveWallets() {
		List<Wallet> wallets = walletRepository.findByActiveTrue();
		return wallets.stream()
				.map(WalletResponse::new)
				.collect(Collectors.toList());
	}

	/**
	 * Transfer credit between wallets (within same admin)
	 */
	public void transferCredit(UUID fromUserId, UUID toUserId, Double amount, String adminEmail) {
		// Find both wallets
		Wallet fromWallet = walletRepository.findByUserId(fromUserId)
				.orElseThrow(() -> new RuntimeException("Source wallet not found for user ID: " + fromUserId));

		Wallet toWallet = walletRepository.findByUserId(toUserId)
				.orElseThrow(() -> new RuntimeException("Destination wallet not found for user ID: " + toUserId));

		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		if (fromWallet.getUser().getAdmin() == null ||
				!fromWallet.getUser().getAdmin().getId().equals(admin.getId()) ||
				toWallet.getUser().getAdmin() == null ||
				!toWallet.getUser().getAdmin().getId().equals(admin.getId())) {
			throw new RuntimeException("Unauthorized: Both users must belong to the same admin");
		}

		// Check sufficient balance
		if (fromWallet.getBalance() < amount) {
			throw new RuntimeException("Insufficient balance in source wallet");
		}

		// Perform transfer
		fromWallet.setBalance(fromWallet.getBalance() - amount);
		toWallet.setBalance(toWallet.getBalance() + amount);

		walletRepository.save(fromWallet);
		walletRepository.save(toWallet);
	}

	/**
	 * Create wallet for a newly registered user (internal use only)
	 * This method is called by UserService during user registration
	 */
	public Wallet createWalletForUser(User user) {
		// Check if wallet already exists
		if (walletRepository.existsByUserId(user.getId())) {
			throw new RuntimeException("Wallet already exists for user: " + user.getEmail());
		}

		Wallet wallet = new Wallet();
		wallet.setUser(user);
		wallet.setBalance(0.0);
		wallet.setActive(true);
		wallet.setTokenizedId(generateTokenizedId(user.getEmail()));
		wallet.setTransferAmount(0.0);

		return walletRepository.save(wallet);
	}

	/**
	 * Bulk transfer credit to all active users based on their wallet's transferAmount
	 * Each user receives the amount specified in their wallet's transferAmount field
	 */
	public BulkTransferResponse bulkTransferCredit(String adminEmail) {
		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		// Find all active wallets with transferAmount > 0 for this admin's users
		List<Wallet> wallets = walletRepository.findActiveWalletsWithTransferAmountByAdminId(admin.getId());

		if (wallets.isEmpty()) {
			throw new RuntimeException("No active wallets with transfer amounts found for your users");
		}

		// Calculate total amount to be transferred
		Double totalAmount = wallets.stream()
				.mapToDouble(Wallet::getTransferAmount)
				.sum();

		// Perform transfers
		List<BulkTransferResponse.WalletTransferDetail> transferDetails = new java.util.ArrayList<>();

		for (Wallet wallet : wallets) {
			Double balanceBefore = wallet.getBalance();
			Double transferAmount = wallet.getTransferAmount();

			wallet.setBalance(wallet.getBalance() + transferAmount);
			walletRepository.save(wallet);

			createCreditTransaction(wallet, transferAmount, balanceBefore, "Bulk credit transfer from admin");

			transferDetails.add(new BulkTransferResponse.WalletTransferDetail(
					wallet.getUser().getEmail(),
					wallet.getTokenizedId(),
					transferAmount,
					wallet.getBalance()
			));
		}

		return new BulkTransferResponse(
				wallets.size(),
				totalAmount,
				transferDetails
		);
	}

	/**
	 * Bulk transfer credit to all active ADMINs based on their wallet's transferAmount
	 * Each ADMIN receives the amount specified in their wallet's transferAmount field
	 * Only SUPER_ADMIN can call this method
	 */
	public BulkTransferResponse bulkTransferCreditToAdmins(String superAdminEmail) {
		User superAdmin = userRepository.findByEmail(superAdminEmail)
				.orElseThrow(() -> new RuntimeException("Super Admin not found: " + superAdminEmail));

		// Find all active ADMIN wallets with transferAmount > 0 for this super admin
		List<Wallet> wallets = walletRepository.findActiveAdminWalletsWithTransferAmountBySuperAdminId(superAdmin.getId());

		if (wallets.isEmpty()) {
			throw new RuntimeException("No active admin wallets with transfer amounts found");
		}

		// Calculate total amount to be transferred
		Double totalAmount = wallets.stream()
				.mapToDouble(Wallet::getTransferAmount)
				.sum();

		// Perform transfers
		List<BulkTransferResponse.WalletTransferDetail> transferDetails = new java.util.ArrayList<>();

		for (Wallet wallet : wallets) {
			Double balanceBefore = wallet.getBalance();
			Double transferAmount = wallet.getTransferAmount();

			wallet.setBalance(wallet.getBalance() + transferAmount);
			walletRepository.save(wallet);

			createCreditTransaction(wallet, transferAmount, balanceBefore, "Bulk credit transfer from super admin");

			transferDetails.add(new BulkTransferResponse.WalletTransferDetail(
					wallet.getUser().getEmail(),
					wallet.getTokenizedId(),
					transferAmount,
					wallet.getBalance()
			));
		}

		return new BulkTransferResponse(
				wallets.size(),
				totalAmount,
				transferDetails
		);
	}

	/**
	 * Update wallet transfer amount (ADMIN only)
	 */
	public WalletResponse updateTransferAmount(UUID userId, Double transferAmount, String adminEmail) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		if (wallet.getUser().getAdmin() == null ||
				!wallet.getUser().getAdmin().getId().equals(admin.getId())) {
			throw new RuntimeException("Unauthorized: Admin doesn't manage this user");
		}

		wallet.setTransferAmount(transferAmount);
		Wallet savedWallet = walletRepository.save(wallet);
		return new WalletResponse(savedWallet);
	}

	/**
	 * Deactivate wallet by user ID (internal use when user is deactivated)
	 */
	public void deactivateWalletByUserId(UUID userId) {
		walletRepository.findByUserId(userId).ifPresent(wallet -> {
			wallet.setActive(false);
			walletRepository.save(wallet);
		});
	}

	/**
	 * Generate unique tokenized ID for wallet
	 */
	private String generateTokenizedId(String userEmail) {
		return "WLT-" + Math.abs(userEmail.hashCode()) + "-" + UUID.randomUUID().toString().substring(0, 8);
	}

	// Helper method to create credit transaction records
	private void createCreditTransaction(Wallet wallet, Double amount, Double balanceBefore, String description) {
		Transaction transaction = new Transaction();
		transaction.setTransactionId(generateTransactionId());
		transaction.setUser(wallet.getUser());
		transaction.setWallet(wallet);
		transaction.setAmount(amount);
		transaction.setStatus(TransactionStatus.COMPLETED);
		transaction.setType(TransactionType.CREDIT_ADDITION);
		transaction.setDescription(description);
		transaction.setBalanceBefore(balanceBefore);
		transaction.setBalanceAfter(wallet.getBalance());
		transaction.setCompletedAt(LocalDateTime.now());

		transactionRepository.save(transaction);
	}

	// Helper method to generate transaction IDs
	private String generateTransactionId() {
		return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}
