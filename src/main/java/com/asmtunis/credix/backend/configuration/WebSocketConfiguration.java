package com.asmtunis.credix.backend.configuration;

import com.asmtunis.credix.backend.features.transaction.websocket.TransactionWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

	private final TransactionWebSocketHandler transactionWebSocketHandler;

	public WebSocketConfiguration(TransactionWebSocketHandler transactionWebSocketHandler) {
		this.transactionWebSocketHandler = transactionWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(transactionWebSocketHandler, "/ws-raw")
				.setAllowedOrigins("*");
	}
}
