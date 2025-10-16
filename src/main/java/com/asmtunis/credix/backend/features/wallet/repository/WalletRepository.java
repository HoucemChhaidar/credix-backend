package com.asmtunis.credix.backend.features.wallet.repository;

import com.asmtunis.credix.backend.features.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
	// Keep email method for JWT authentication
	Optional<Wallet> findByUserEmail(String email);

	// Add ID-based methods
	Optional<Wallet> findByUserId(UUID userId);

	Optional<Wallet> findByTokenizedId(String tokenizedId);

	List<Wallet> findByIsActiveTrue();

	boolean existsByUserId(UUID userId);

	@Query("SELECT w FROM Wallet w WHERE w.user.admin.id = :adminId")
	List<Wallet> findByUserAdminId(@Param("adminId") UUID adminId);
}
