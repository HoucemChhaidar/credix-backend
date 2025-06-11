package com.asmtunis.credix.backend.wallet.repository;

import com.asmtunis.credix.backend.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
	Optional<Wallet> findByUserEmail(String email);

	Optional<Wallet> findByTokenizedId(String tokenizedId);

	List<Wallet> findByIsActiveTrue();

	boolean existsByUserEmail(String email);

	@Query("SELECT w FROM Wallet w WHERE w.user.corporate.email = :corporateEmail")
	List<Wallet> findByUserCorporateEmail(@Param("corporateEmail") String corporateEmail);
}
