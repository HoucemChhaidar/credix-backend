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

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize annotations
public class SecurityConfiguration {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						// Public endpoints
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers("/api/wallets/tokenized/**").permitAll() // For barcode payments

						// Swagger endpoints
						.requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()

						// Admin endpoints
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/wallets/active").hasRole("ADMIN")

						// Corporate endpoints
						.requestMatchers("/api/wallets").hasRole("CORPORATE")
						.requestMatchers("/api/wallets/user/**").hasRole("CORPORATE")
						.requestMatchers("/api/wallets/corporate").hasRole("CORPORATE")
						.requestMatchers("/api/wallets/add-credit").hasRole("CORPORATE")
						.requestMatchers("/api/wallets/balance/**").hasRole("CORPORATE")
						.requestMatchers("/api/wallets/toggle-status/**").hasRole("CORPORATE")
						.requestMatchers("/api/wallets/transfer").hasRole("CORPORATE")

						// Vendor endpoints (for payment processing)
						.requestMatchers("/api/wallets/deduct/**").hasAnyRole("VENDOR", "ADMIN")

						// User endpoints (own wallet access)
						.requestMatchers("/api/wallets/my-wallet").hasAnyRole("USER", "VENDOR", "ADMIN")
						.requestMatchers("/api/wallets/my-balance").hasAnyRole("USER", "VENDOR", "ADMIN")

						// All other requests require authentication
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}