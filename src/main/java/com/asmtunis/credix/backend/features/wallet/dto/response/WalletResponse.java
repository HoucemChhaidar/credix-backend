package com.asmtunis.credix.backend.features.wallet.dto.response;

import com.asmtunis.credix.backend.features.wallet.entity.Wallet;

import java.time.LocalDateTime;

public class WalletResponse {
	private String tokenizedId;
	private Double balance;
	private Boolean active;
	private Double transferAmount;
	private String userEmail;
	private LocalDateTime createdAt;

	public WalletResponse() {}

	public WalletResponse(Wallet wallet) {
		this.tokenizedId = wallet.getTokenizedId();
		this.balance = wallet.getBalance();
		this.active = wallet.getActive();
		this.transferAmount = wallet.getTransferAmount();
		this.userEmail = wallet.getUser().getEmail();
		this.createdAt = wallet.getCreatedAt();
	}

	public String getTokenizedId() {return tokenizedId;}

	public void setTokenizedId(String tokenizedId) {this.tokenizedId = tokenizedId;}

	public Double getBalance() {return balance;}

	public void setBalance(Double balance) {this.balance = balance;}

	public Boolean getActive() {return active;}

	public void setActive(Boolean active) {this.active = active;}

	public Double getTransferAmount() {return transferAmount;}

	public void setTransferAmount(Double transferAmount) {this.transferAmount = transferAmount;}

	public String getUserEmail() {return userEmail;}

	public void setUserEmail(String userEmail) {this.userEmail = userEmail;}

	public LocalDateTime getCreatedAt() {return createdAt;}

	public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}
