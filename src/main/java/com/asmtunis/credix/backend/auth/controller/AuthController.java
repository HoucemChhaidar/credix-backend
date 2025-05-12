package com.asmtunis.credix.backend.auth.controller;

import com.asmtunis.credix.backend.auth.dto.AuthResponse;
import com.asmtunis.credix.backend.auth.dto.LoginRequest;
import com.asmtunis.credix.backend.auth.dto.RegisterRequest;
import com.asmtunis.credix.backend.auth.model.User;
import com.asmtunis.credix.backend.auth.repository.UserRepository;
import com.asmtunis.credix.backend.auth.service.JwtService;
import com.asmtunis.credix.backend.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AuthController(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
		if (userRepository.findByEmail(request.email).isPresent()) {
			ApiResponse<String> response = new ApiResponse<>("Email already taken.", HttpStatus.BAD_REQUEST.value(), null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		User newUser = new User();
		newUser.setEmail(request.email);
		newUser.setPassword(passwordEncoder.encode(request.password));
		newUser.setRole(request.role);

		userRepository.save(newUser);

		ApiResponse<String> response = new ApiResponse<>("User registered successfully!", HttpStatus.OK.value(), "Registered");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
		return userRepository.findByEmail(request.email).filter(user -> passwordEncoder.matches(request.password, user.getPassword())).map(user -> {
			AuthResponse authResponse = new AuthResponse(jwtService.generateToken(user));
			ApiResponse<AuthResponse> response = new ApiResponse<>("Login successful", HttpStatus.OK.value(), authResponse);
			return ResponseEntity.ok(response);
		}).orElseGet(() -> {
			ApiResponse<AuthResponse> error = new ApiResponse<>("Invalid email or password", HttpStatus.UNAUTHORIZED.value(), null);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		});
	}

	@GetMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout() {
		ApiResponse<String> response = new ApiResponse<>("You are logged out. Please delete your token client-side.", HttpStatus.OK.value(), "Logged out");
		return ResponseEntity.ok(response);
	}
}
