package com.asmtunis.credix.backend.wallet.repository;

import com.asmtunis.credix.backend.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
	// Keep email method for JWT authentication
	Optional<Wallet> findByUserEmail(String email);

	// Add ID-based methods
	Optional<Wallet> findByUserId(Long userId);

	Optional<Wallet> findByTokenizedId(String tokenizedId);

	List<Wallet> findByIsActiveTrue();

	boolean existsByUserId(Long userId);

	@Query("SELECT w FROM Wallet w WHERE w.user.corporate.id = :corporateId")
	List<Wallet> findByUserCorporateId(@Param("corporateId") Long corporateId);
}