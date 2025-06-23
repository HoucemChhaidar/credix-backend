package com.asmtunis.credix.backend.features.wallet.dto.response;

import com.asmtunis.credix.backend.features.wallet.entity.Wallet;

import java.time.LocalDateTime;

public class WalletResponse {
	private String tokenizedId;
	private Double balance;
	private Boolean isActive;
	private String userEmail;
	private LocalDateTime createdAt;

	public WalletResponse() {}

	public WalletResponse(Wallet wallet) {
		this.tokenizedId = wallet.getTokenizedId();
		this.balance = wallet.getBalance();
		this.isActive = wallet.getIsActive();
		this.userEmail = wallet.getUser().getEmail();
		this.createdAt = wallet.getCreatedAt();
	}

	public String getTokenizedId() {return tokenizedId;}

	public void setTokenizedId(String tokenizedId) {this.tokenizedId = tokenizedId;}

	public Double getBalance() {return balance;}

	public void setBalance(Double balance) {this.balance = balance;}

	public Boolean getIsActive() {return isActive;}

	public void setIsActive(Boolean isActive) {this.isActive = isActive;}

	public String getUserEmail() {return userEmail;}

	public void setUserEmail(String userEmail) {this.userEmail = userEmail;}

	public LocalDateTime getCreatedAt() {return createdAt;}

	public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}