package com.asmtunis.credix.backend.configuration;

import com.asmtunis.credix.backend.features.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableMethodSecurity(prePostEnabled = true) public class SecurityConfiguration {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth
						// Public endpoints
						.requestMatchers("/auth/**").permitAll().requestMatchers("/api/users/bootstrap").permitAll()
						.requestMatchers("/api/wallets/tokenized/**").permitAll()

						.requestMatchers("/api/transactions/payment/process").permitAll()
						.requestMatchers("/ws/**", "/ws-raw/**", "/ws-sockjs/**").permitAll()

						// Swagger endpoints
						.requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()

						// SUPER_ADMIN endpoints
						.requestMatchers("/api/users/role/**").hasRole("SUPER_ADMIN")

						// ADMIN and SUPER_ADMIN endpoints
						.requestMatchers("/api/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN").requestMatchers("/api/wallets/active")
						.hasAnyRole("ADMIN", "SUPER_ADMIN").requestMatchers("/api/wallets").hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers("/api/wallets/user/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers("/api/wallets/add-credit").hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers("/api/wallets/balance/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers("/api/wallets/toggle-status/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers("/api/wallets/transfer").hasAnyRole("ADMIN", "SUPER_ADMIN")

						// User endpoints (own wallet access)
						.requestMatchers("/api/wallets/my-wallet").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
						.requestMatchers("/api/wallets/my-balance").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")

						// All other requests require authentication
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
