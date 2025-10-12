package com.asmtunis.credix.backend.features.transaction.service;

import com.asmtunis.credix.backend.features.transaction.dto.response.TransactionNotification;
import com.asmtunis.credix.backend.features.transaction.websocket.TransactionWebSocketHandler;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WebSocketNotificationService {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

	private final TransactionWebSocketHandler webSocketHandler;

	public WebSocketNotificationService(TransactionWebSocketHandler webSocketHandler) {
		this.webSocketHandler = webSocketHandler;
	}

	/**
	 * Send transaction notification to a specific user
	 */
	public void sendTransactionNotification(String userEmail, TransactionNotification notification) {
		try {
			logger.info("[Notification] Sending to user: {}", userEmail);
			logger.info("[Notification] Transaction ID: {}, Amount: {}, Status: {}",
					notification.transactionId(),
					notification.amount());

			webSocketHandler.sendNotificationToUser(userEmail, notification);

			logger.info("[Notification] Successfully sent to: {}", userEmail);
		} catch (Exception e) {
			logger.error("[Notification] Failed to send to {}: {}", userEmail, e.getMessage(), e);
		}
	}
}
