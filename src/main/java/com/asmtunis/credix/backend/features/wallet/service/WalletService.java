package com.asmtunis.credix.backend.features.wallet.service;

import com.asmtunis.credix.backend.features.auth.entity.User;
import com.asmtunis.credix.backend.features.auth.repository.UserRepository;
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
	public WalletResponse createWallet(CreateWalletRequest request, String corporateEmail) {

		if (walletRepository.existsByUserId(request.getUserId())) {
			throw new RuntimeException("Wallet already exists for user ID: " + request.getUserId());
		}

		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		if (user.getCorporate() == null || !user.getCorporate().getId().equals(corporate.getId())) {
			throw new RuntimeException("Unauthorized: User doesn't belong to this corporate");
		}

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
	 * Get all wallets managed by a corporate (using corporate email from JWT)
	 */
	@Transactional(readOnly = true)
	public List<WalletResponse> getWalletsByCorporate(String corporateEmail) {

		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		List<Wallet> wallets = walletRepository.findByUserCorporateId(corporate.getId());
		return wallets.stream()
				.map(WalletResponse::new)
				.collect(Collectors.toList());
	}

	/**
	 * Add credit to a user's wallet (ID-based)
	 */
	public WalletResponse addCredit(AddCreditRequest request, String corporateEmail) {

		Wallet wallet = walletRepository.findByUserId(request.getUserId())
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + request.getUserId()));

		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		if (wallet.getUser().getCorporate() == null ||
				!wallet.getUser().getCorporate().getId().equals(corporate.getId())) {
			throw new RuntimeException("Unauthorized: Corporate doesn't manage this user");
		}

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
	public WalletResponse toggleWalletStatus(UUID userId, Boolean isActive, String corporateEmail) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

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
	public void transferCredit(UUID fromUserId, UUID toUserId, Double amount, String corporateEmail) {

		Wallet fromWallet = walletRepository.findByUserId(fromUserId)
				.orElseThrow(() -> new RuntimeException("Source wallet not found for user ID: " + fromUserId));

		Wallet toWallet = walletRepository.findByUserId(toUserId)
				.orElseThrow(() -> new RuntimeException("Destination wallet not found for user ID: " + toUserId));

		User corporate = userRepository.findByEmail(corporateEmail)
				.orElseThrow(() -> new RuntimeException("Corporate not found: " + corporateEmail));

		if (fromWallet.getUser().getCorporate() == null ||
				!fromWallet.getUser().getCorporate().getId().equals(corporate.getId()) ||
				toWallet.getUser().getCorporate() == null ||
				!toWallet.getUser().getCorporate().getId().equals(corporate.getId())) {
			throw new RuntimeException("Unauthorized: Both users must belong to the same corporate");
		}

		if (fromWallet.getBalance() < amount) {
			throw new RuntimeException("Insufficient balance in source wallet");
		}

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