package com.asmtunis.credix.backend.features.transaction.repository;

import com.asmtunis.credix.backend.features.transaction.entity.Transaction;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionStatus;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Optional<Transaction> findByTransactionId(String transactionId);

	Optional<Transaction> findByBarcodeData(String barcodeData);

	List<Transaction> findByUserId(UUID userId);

	List<Transaction> findByWalletId(Long walletId);

	List<Transaction> findByStatus(TransactionStatus status);

	List<Transaction> findByType(TransactionType type);

	@Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
	List<Transaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

	@Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId ORDER BY t.createdAt DESC")
	List<Transaction> findByWalletIdOrderByCreatedAtDesc(@Param("walletId") Long walletId);

	@Query("SELECT t FROM Transaction t WHERE t.user.admin.id = :adminId ORDER BY t.createdAt DESC")
	List<Transaction> findByAdminIdOrderByCreatedAtDesc(@Param("adminId") UUID adminId);

	@Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.status = :status")
	List<Transaction> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") TransactionStatus status);

	@Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
	List<Transaction> findByDateRange(
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	@Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
	List<Transaction> findByUserIdAndDateRange(
			@Param("userId") UUID userId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate
	);

	@Query("SELECT t FROM Transaction t WHERE t.barcodeExpiry < :now AND t.status = 'PENDING'")
	List<Transaction> findExpiredPendingTransactions(@Param("now") LocalDateTime now);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.status = 'COMPLETED' AND t.type = 'PAYMENT'")
	Double getTotalSpentByUser(@Param("userId") UUID userId);

	@Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.status = 'COMPLETED'")
	Long getCompletedTransactionCountByUser(@Param("userId") UUID userId);
}
