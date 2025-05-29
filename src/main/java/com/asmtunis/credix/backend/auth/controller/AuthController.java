package com.asmtunis.credix.backend.auth.controller;

import com.asmtunis.credix.backend.auth.dto.request.LoginRequest;
import com.asmtunis.credix.backend.auth.dto.request.RegisterRequest;
import com.asmtunis.credix.backend.auth.dto.response.AuthResponse;
import com.asmtunis.credix.backend.auth.dto.response.UserResponse;
import com.asmtunis.credix.backend.auth.service.AuthService;
import com.asmtunis.credix.backend.common.dto.ResponseWrapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<ResponseWrapper<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
		if (authService.isEmailTaken(request.getEmail())) {
			ResponseWrapper<UserResponse> response = new ResponseWrapper<>(
					HttpStatus.BAD_REQUEST.value(),
					"Email already taken.",
					null
			);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		UserResponse userResponse = authService.register(request);
		ResponseWrapper<UserResponse> response = new ResponseWrapper<>(
				HttpStatus.OK.value(),
				"User registered successfully!",
				userResponse
		);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseWrapper<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request)
				.map(authResponse -> {
					ResponseWrapper<AuthResponse> response = new ResponseWrapper<>(
							HttpStatus.OK.value(),
							"Login successful",
							authResponse
					);
					return ResponseEntity.ok(response);
				})
				.orElseGet(() -> {
					ResponseWrapper<AuthResponse> error = new ResponseWrapper<>(
							HttpStatus.UNAUTHORIZED.value(),
							"Invalid email or password",
							null
					);
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
				});
	}

	@GetMapping("/logout")
	public ResponseEntity<ResponseWrapper<String>> logout() {
		ResponseWrapper<String> response = new ResponseWrapper<>(
				HttpStatus.OK.value(),
				"You are logged out. Please delete your token client-side.",
				"Logged out"
		);
		return ResponseEntity.ok(response);
	}
}