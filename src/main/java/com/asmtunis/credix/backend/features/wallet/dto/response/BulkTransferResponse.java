package com.asmtunis.credix.backend.features.wallet.dto.response;

import java.util.List;
import java.util.Map;

public class BulkTransferResponse {
	private Integer totalWalletsUpdated;
	private Double totalAmountTransferred;
	private List<WalletTransferDetail> transfers;

	public BulkTransferResponse() {
	}

	public BulkTransferResponse(Integer totalWalletsUpdated, Double totalAmountTransferred, List<WalletTransferDetail> transfers) {
		this.totalWalletsUpdated = totalWalletsUpdated;
		this.totalAmountTransferred = totalAmountTransferred;
		this.transfers = transfers;
	}

	public Integer getTotalWalletsUpdated() {
		return totalWalletsUpdated;
	}

	public void setTotalWalletsUpdated(Integer totalWalletsUpdated) {
		this.totalWalletsUpdated = totalWalletsUpdated;
	}

	public Double getTotalAmountTransferred() {
		return totalAmountTransferred;
	}

	public void setTotalAmountTransferred(Double totalAmountTransferred) {
		this.totalAmountTransferred = totalAmountTransferred;
	}

	public List<WalletTransferDetail> getTransfers() {
		return transfers;
	}

	public void setTransfers(List<WalletTransferDetail> transfers) {
		this.transfers = transfers;
	}

	public static class WalletTransferDetail {
		private String userEmail;
		private String tokenizedId;
		private Double amountTransferred;
		private Double newBalance;

		public WalletTransferDetail() {
		}

		public WalletTransferDetail(String userEmail, String tokenizedId, Double amountTransferred, Double newBalance) {
			this.userEmail = userEmail;
			this.tokenizedId = tokenizedId;
			this.amountTransferred = amountTransferred;
			this.newBalance = newBalance;
		}

		public String getUserEmail() {
			return userEmail;
		}

		public void setUserEmail(String userEmail) {
			this.userEmail = userEmail;
		}

		public String getTokenizedId() {
			return tokenizedId;
		}

		public void setTokenizedId(String tokenizedId) {
			this.tokenizedId = tokenizedId;
		}

		public Double getAmountTransferred() {
			return amountTransferred;
		}

		public void setAmountTransferred(Double amountTransferred) {
			this.amountTransferred = amountTransferred;
		}

		public Double getNewBalance() {
			return newBalance;
		}

		public void setNewBalance(Double newBalance) {
			this.newBalance = newBalance;
		}
	}
}
