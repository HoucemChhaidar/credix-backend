package com.asmtunis.credix.backend.features.transaction.dto.response;

import com.asmtunis.credix.backend.features.transaction.entity.Transaction;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionStatus;
import com.asmtunis.credix.backend.features.transaction.entity.TransactionType;

import java.time.LocalDateTime;

public class TransactionResponse {
	private Long id;
	private String transactionId;
	private String userEmail;
	private String walletTokenizedId;
	private Double amount;
	private TransactionStatus status;
	private TransactionType type;
	private String merchantName;
	private String merchantId;
	private String description;
	private String failureReason;
	private Double balanceBefore;
	private Double balanceAfter;
	private LocalDateTime createdAt;
	private LocalDateTime completedAt;

	public TransactionResponse() {
	}

	public TransactionResponse(Transaction transaction) {
		this.id = transaction.getId();
		this.transactionId = transaction.getTransactionId();
		this.userEmail = transaction.getUser().getEmail();
		this.walletTokenizedId = transaction.getWallet().getTokenizedId();
		this.amount = transaction.getAmount();
		this.status = transaction.getStatus();
		this.type = transaction.getType();
		this.merchantName = transaction.getMerchantName();
		this.merchantId = transaction.getMerchantId();
		this.description = transaction.getDescription();
		this.failureReason = transaction.getFailureReason();
		this.balanceBefore = transaction.getBalanceBefore();
		this.balanceAfter = transaction.getBalanceAfter();
		this.createdAt = transaction.getCreatedAt();
		this.completedAt = transaction.getCompletedAt();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getWalletTokenizedId() {
		return walletTokenizedId;
	}

	public void setWalletTokenizedId(String walletTokenizedId) {
		this.walletTokenizedId = walletTokenizedId;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public Double getBalanceBefore() {
		return balanceBefore;
	}

	public void setBalanceBefore(Double balanceBefore) {
		this.balanceBefore = balanceBefore;
	}

	public Double getBalanceAfter() {
		return balanceAfter;
	}

	public void setBalanceAfter(Double balanceAfter) {
		this.balanceAfter = balanceAfter;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}
}
