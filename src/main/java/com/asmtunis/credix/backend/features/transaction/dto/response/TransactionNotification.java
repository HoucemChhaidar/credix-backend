package com.asmtunis.credix.backend.features.transaction.dto.response;

import java.time.LocalDateTime;

public record TransactionNotification(
		String transactionId,
		LocalDateTime timestamp,
		String merchantName,
		Double amount
) {}
