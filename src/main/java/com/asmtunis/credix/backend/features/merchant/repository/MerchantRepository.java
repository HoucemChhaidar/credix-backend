package com.asmtunis.credix.backend.features.merchant.repository;

import com.asmtunis.credix.backend.features.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
	Optional<Merchant> findByMerchantId(String merchantId);

	Optional<Merchant> findByEmail(String email);

	boolean existsByMerchantId(String merchantId);
}
