package com.asmtunis.credix.backend.wallet.service;

import com.asmtunis.credix.backend.auth.entity.User;
import com.asmtunis.credix.backend.auth.repository.UserRepository;
import com.asmtunis.credix.backend.wallet.dto.request.AddCreditRequest;
import com.asmtunis.credix.backend.wallet.dto.request.CreateWalletRequest;
import com.asmtunis.credix.backend.wallet.dto.response.WalletResponse;
import com.asmtunis.credix.backend.wallet.entity.Wallet;
import com.asmtunis.credix.backend.wallet.repository.WalletRepository;
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
	public WalletResponse createWallet(CreateWalletRequest request, String corporateEmail) {
		// Check if wallet already exists for this user
		if (walletRepository.existsByUserId(request.getUserId())) {
			throw new RuntimeException("Wallet already exists for user ID: " + request.getUserId());
		}

		// Find the user by ID
		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

		// Get the corporate user from JWT email
		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		// Verify the user belongs to this corporate
		if (user.getCorporate() == null || !user.getCorporate().getId().equals(corporate.getId())) {
			throw new RuntimeException("Unauthorized: User doesn't belong to this corporate");
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
	public WalletResponse getWalletByUserId(Long userId) {
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
	 * Get all wallets managed by a corporate (using corporate email from JWT)
	 */
	@Transactional(readOnly = true)
	public List<WalletResponse> getWalletsByCorporate(String corporateEmail) {
		// Get corporate user
		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		// Find wallets by corporate ID
		List<Wallet> wallets = walletRepository.findByUserCorporateId(corporate.getId());
		return wallets.stream()
				.map(WalletResponse::new)
				.collect(Collectors.toList());
	}

	/**
	 * Add credit to a user's wallet (ID-based)
	 */
	public WalletResponse addCredit(AddCreditRequest request, String corporateEmail) {
		// Find wallet by user ID
		Wallet wallet = walletRepository.findByUserId(request.getUserId())
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + request.getUserId()));

		// Get corporate user from JWT
		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		// Verify corporate manages this user
		if (wallet.getUser().getCorporate() == null ||
				!wallet.getUser().getCorporate().getId().equals(corporate.getId())) {
			throw new RuntimeException("Unauthorized: Corporate doesn't manage this user");
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
	public Double getBalanceByUserId(Long userId) {
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
	public WalletResponse deductCredit(Long userId, Double amount, String description) {
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
	public WalletResponse toggleWalletStatus(Long userId, Boolean isActive, String corporateEmail) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

		// Get corporate user from JWT
		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		// Verify corporate manages this user
		if (wallet.getUser().getCorporate() == null ||
				!wallet.getUser().getCorporate().getId().equals(corporate.getId())) {
			throw new RuntimeException("Unauthorized: Corporate doesn't manage this user");
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
	 * Transfer credit between wallets (within same corporate)
	 */
	public void transferCredit(Long fromUserId, Long toUserId, Double amount, String corporateEmail) {
		// Find both wallets
		Wallet fromWallet = walletRepository.findByUserId(fromUserId)
				.orElseThrow(() -> new RuntimeException("Source wallet not found for user ID: " + fromUserId));

		Wallet toWallet = walletRepository.findByUserId(toUserId)
				.orElseThrow(() -> new RuntimeException("Destination wallet not found for user ID: " + toUserId));

		// Get corporate user
		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		// Verify both users belong to the same corporate
		if (fromWallet.getUser().getCorporate() == null ||
				!fromWallet.getUser().getCorporate().getId().equals(corporate.getId()) ||
				toWallet.getUser().getCorporate() == null ||
				!toWallet.getUser().getCorporate().getId().equals(corporate.getId())) {
			throw new RuntimeException("Unauthorized: Both users must belong to the same corporate");
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