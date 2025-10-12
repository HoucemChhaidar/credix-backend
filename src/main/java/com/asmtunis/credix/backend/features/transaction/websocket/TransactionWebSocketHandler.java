package com.asmtunis.credix.backend.features.transaction.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TransactionWebSocketHandler extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(TransactionWebSocketHandler.class);
	private final ObjectMapper objectMapper;

	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	public TransactionWebSocketHandler() {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String userEmail = extractUserEmail(session);

		if (userEmail != null) {
			sessions.put(userEmail, session);
			logger.info("[WebSocket] User connected: {} (Total sessions: {})", userEmail, sessions.size());

			// Send connection confirmation
			sendMessage(session, Map.of(
					"type", "connection",
					"message", "Connected successfully",
					"userEmail", userEmail
			));
		} else {
			logger.warn("[WebSocket] Connection rejected: missing userEmail parameter");
			session.close(CloseStatus.BAD_DATA);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String userEmail = extractUserEmail(session);

		if (userEmail != null) {
			sessions.remove(userEmail);
			logger.info("[WebSocket] User disconnected: {} (Total sessions: {})", userEmail, sessions.size());
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		logger.debug("[WebSocket] Received message: {}", message.getPayload());
		// Echo back for testing/debugging
		sendMessage(session, Map.of("echo", message.getPayload()));
	}

	/**
	 * Send notification to a specific user by email
	 *
	 * @param userEmail The user's email address
	 * @param message   The message object to send (will be serialized to JSON)
	 */
	public void sendNotificationToUser(String userEmail, Object message) {
		WebSocketSession session = sessions.get(userEmail);

		if (session != null && session.isOpen()) {
			try {
				String json = objectMapper.writeValueAsString(message);
				session.sendMessage(new TextMessage(json));
				logger.info("[WebSocket] Notification sent to: {}", userEmail);
			} catch (IOException e) {
				logger.error("[WebSocket] Failed to send notification to {}: {}", userEmail, e.getMessage());
			}
		} else {
			logger.warn("[WebSocket] No active session for user: {}", userEmail);
		}
	}

	/**
	 * Get count of active WebSocket sessions
	 */
	public int getActiveSessionCount() {
		return sessions.size();
	}

	/**
	 * Check if a user has an active WebSocket connection
	 */
	public boolean isUserConnected(String userEmail) {
		WebSocketSession session = sessions.get(userEmail);
		return session != null && session.isOpen();
	}

	private void sendMessage(WebSocketSession session, Object message) throws IOException {
		String json = objectMapper.writeValueAsString(message);
		session.sendMessage(new TextMessage(json));
	}

	private String extractUserEmail(WebSocketSession session) {
		String query = session.getUri().getQuery();
		if (query != null && query.contains("userEmail=")) {
			String[] params = query.split("&");
			for (String param : params) {
				if (param.startsWith("userEmail=")) {
					return param.substring("userEmail=".length());
				}
			}
		}
		return null;
	}
}
