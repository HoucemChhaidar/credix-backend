package com.asmtunis.credix.backend.features.wallet.service;

import com.asmtunis.credix.backend.features.user.entity.User;
import com.asmtunis.credix.backend.features.user.repository.UserRepository;
import com.asmtunis.credix.backend.features.wallet.dto.request.AddCreditRequest;
import com.asmtunis.credix.backend.features.wallet.dto.request.CreateWalletRequest;
import com.asmtunis.credix.backend.features.wallet.dto.response.WalletResponse;
import com.asmtunis.credix.backend.features.wallet.entity.Wallet;
import com.asmtunis.credix.backend.features.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletService {
	private final WalletRepository walletRepository;
	private final UserRepository userRepository;

	public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
		this.walletRepository = walletRepository;
		this.userRepository = userRepository;
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

		// Add credit to wallet
		wallet.setBalance(wallet.getBalance() + request.getAmount());

		Wallet savedWallet = walletRepository.save(wallet);
		return new WalletResponse(savedWallet);
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
	 * Get balance by user email (for JWT scenarios)
	 */
	@Transactional(readOnly = true)
	public Double getBalanceByUserEmail(String userEmail) {
		Wallet wallet = walletRepository.findByUserEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userEmail));
		return wallet.getBalance();
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
	public WalletResponse toggleWalletStatus(UUID userId, Boolean isActive, String adminEmail) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new RuntimeException("Admin not found: " + adminEmail));

		if (wallet.getUser().getAdmin() == null ||
				!wallet.getUser().getAdmin().getId().equals(admin.getId())) {
			throw new RuntimeException("Unauthorized: Admin doesn't manage this user");
		}

		wallet.setIsActive(isActive);
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
		List<Wallet> wallets = walletRepository.findByIsActiveTrue();
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
	 * Generate unique tokenized ID for wallet
	 */
	private String generateTokenizedId(String userEmail) {
		return "WLT-" + Math.abs(userEmail.hashCode()) + "-" + UUID.randomUUID().toString().substring(0, 8);
	}
}
