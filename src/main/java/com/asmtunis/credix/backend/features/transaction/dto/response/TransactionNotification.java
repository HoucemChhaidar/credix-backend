package com.asmtunis.credix.backend.features.transaction.dto.response;

import java.time.LocalDateTime;

public record TransactionNotification(
		String transactionId,
		LocalDateTime date,
		String merchantName,
		Double amount
) {
}
