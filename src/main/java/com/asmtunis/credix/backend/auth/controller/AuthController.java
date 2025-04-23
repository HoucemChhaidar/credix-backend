package com.asmtunis.credix.backend.auth.controller;

import com.asmtunis.credix.backend.auth.dto.LoginRequest;
import com.asmtunis.credix.backend.auth.dto.RegisterRequest;
import com.asmtunis.credix.backend.auth.model.User;
import com.asmtunis.credix.backend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@PostMapping("/register")
	public String register(@RequestBody RegisterRequest request) {
		if (userRepository.findByUsername(request.username).isPresent()) {
			return "Username already taken.";
		}
		User newUser = new User();
		newUser.setUsername(request.username);
		newUser.setPassword(passwordEncoder.encode(request.password));
		userRepository.save(newUser);
		return "User registered successfully!";
	}

	@PostMapping("/login")
	public String login(@RequestBody LoginRequest request) {
		return userRepository.findByUsername(request.username).filter(user -> passwordEncoder.matches(request.password, user.getPassword())).map(user -> "Login successful!").orElse("Invalid username or password");
	}
}
