package com.asmtunis.credix.backend.auth.controller;

import com.asmtunis.credix.backend.auth.dto.request.LoginRequest;
import com.asmtunis.credix.backend.auth.dto.request.RegisterRequest;
import com.asmtunis.credix.backend.auth.dto.response.AuthResponse;
import com.asmtunis.credix.backend.auth.dto.response.UserResponse;
import com.asmtunis.credix.backend.auth.service.AuthService;
import com.asmtunis.credix.backend.common.dto.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@Operation(summary = "Register a new user", description = "Create a new user account with email, password, and role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User registered successfully",
					content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
			@ApiResponse(responseCode = "400", description = "Email already taken or validation error",
					content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
	})
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
	@Operation(summary = "User login", description = "Authenticate user and return JWT token")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Login successful",
					content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
			@ApiResponse(responseCode = "401", description = "Invalid email or password",
					content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
	})
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
	@Operation(summary = "User logout", description = "Logout user (client should delete JWT token)")
	@ApiResponse(responseCode = "200", description = "Logout successful")
	public ResponseEntity<ResponseWrapper<String>> logout() {
		ResponseWrapper<String> response = new ResponseWrapper<>(
				HttpStatus.OK.value(),
				"You are logged out. Please delete your token client-side.",
				"Logged out"
		);
		return ResponseEntity.ok(response);
	}
}