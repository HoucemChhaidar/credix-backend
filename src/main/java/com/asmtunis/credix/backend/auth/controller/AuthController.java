package com.asmtunis.credix.backend.auth.controller;

import com.asmtunis.credix.backend.auth.dto.AuthResponse;
import com.asmtunis.credix.backend.auth.dto.LoginRequest;
import com.asmtunis.credix.backend.auth.dto.RegisterRequest;
import com.asmtunis.credix.backend.auth.model.User;
import com.asmtunis.credix.backend.auth.repository.UserRepository;
import com.asmtunis.credix.backend.auth.service.JwtService;
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
	public String register(@RequestBody RegisterRequest request) {
		if (userRepository.findByUsername(request.username).isPresent()) {
			return "Username already taken.";
		}

		User newUser = new User();
		newUser.setUsername(request.username);
		newUser.setPassword(passwordEncoder.encode(request.password));
		newUser.setRole(request.role);

		userRepository.save(newUser);
		return "User registered successfully!";
	}

	@PostMapping("/login")
	public AuthResponse login(@RequestBody LoginRequest request) {
		return userRepository.findByUsername(request.username)
						.filter(user -> passwordEncoder.matches(request.password, user.getPassword()))
						.map(user -> new AuthResponse(jwtService.generateToken(user)))
						.orElseThrow(() -> new RuntimeException("Invalid username or password"));
	}

	@GetMapping("/logout")
	public ResponseEntity<String> logout() {
		return ResponseEntity.ok("You are logged out. Please delete your token client-side.");
	}
}
