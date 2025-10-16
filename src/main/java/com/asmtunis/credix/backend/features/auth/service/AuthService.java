package com.asmtunis.credix.backend.features.auth.service;

import com.asmtunis.credix.backend.features.auth.dto.request.LoginRequest;
import com.asmtunis.credix.backend.features.auth.dto.response.AuthResponse;
import com.asmtunis.credix.backend.features.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final BCryptPasswordEncoder passwordEncoder;

	public AuthService(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	public Optional<AuthResponse> login(LoginRequest request) {
		return userRepository.findByEmail(request.getEmail())
				.filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
				.map(user -> new AuthResponse(jwtService.generateToken(user)));
	}
}
