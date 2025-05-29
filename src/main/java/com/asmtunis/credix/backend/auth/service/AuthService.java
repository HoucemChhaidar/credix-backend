package com.asmtunis.credix.backend.auth.service;

import com.asmtunis.credix.backend.auth.dto.request.LoginRequest;
import com.asmtunis.credix.backend.auth.dto.request.RegisterRequest;
import com.asmtunis.credix.backend.auth.dto.response.AuthResponse;
import com.asmtunis.credix.backend.auth.dto.response.UserResponse;
import com.asmtunis.credix.backend.auth.entity.User;
import com.asmtunis.credix.backend.auth.repository.UserRepository;
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

	public boolean isEmailTaken(String email) {
		return userRepository.findByEmail(email).isPresent();
	}

	public UserResponse register(RegisterRequest request) {
		User newUser = new User();
		newUser.setEmail(request.getEmail());
		newUser.setPassword(passwordEncoder.encode(request.getPassword()));
		newUser.setRole(request.getRole());

		User savedUser = userRepository.save(newUser);
		return new UserResponse(savedUser);
	}

	public Optional<AuthResponse> login(LoginRequest request) {
		return userRepository.findByEmail(request.getEmail())
				.filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
				.map(user -> new AuthResponse(jwtService.generateToken(user)));
	}
}