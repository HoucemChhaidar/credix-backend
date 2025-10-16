package com.asmtunis.credix.backend.features.user.service;

import com.asmtunis.credix.backend.features.user.dto.request.CreateUserRequest;
import com.asmtunis.credix.backend.features.user.dto.request.UpdateUserRequest;
import com.asmtunis.credix.backend.features.user.dto.response.UserListResponse;
import com.asmtunis.credix.backend.features.user.entity.Role;
import com.asmtunis.credix.backend.features.user.entity.User;
import com.asmtunis.credix.backend.features.user.repository.UserRepository;
import com.asmtunis.credix.backend.features.wallet.entity.Wallet;
import com.asmtunis.credix.backend.features.wallet.repository.WalletRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final WalletRepository walletRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, WalletRepository walletRepository) {
		this.userRepository = userRepository;
		this.walletRepository = walletRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	@Transactional
	public UserListResponse createUser(CreateUserRequest request) {
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("Email already taken");
		}

		User newUser = new User();
		newUser.setEmail(request.getEmail());
		newUser.setPassword(passwordEncoder.encode(request.getPassword()));
		newUser.setRole(request.getRole());
		newUser.setActive(true);

		if (request.getAdminId() != null) {
			User admin = userRepository.findById(request.getAdminId())
					.orElseThrow(() -> new RuntimeException("Admin not found"));

			// Validate admin role hierarchy
			if (request.getRole() == Role.USER && admin.getRole() != Role.ADMIN) {
				throw new RuntimeException("USER must be assigned to an ADMIN");
			}
			if (request.getRole() == Role.ADMIN && admin.getRole() != Role.SUPER_ADMIN) {
				throw new RuntimeException("ADMIN must be assigned to a SUPER_ADMIN");
			}

			newUser.setAdmin(admin);
		}

		User savedUser = userRepository.save(newUser);

		if (savedUser.getRole() == Role.USER) {
			Wallet wallet = new Wallet();
			wallet.setUser(savedUser);
			wallet.setBalance(0.0);
			wallet.setIsActive(true);
			walletRepository.save(wallet);
		}

		return new UserListResponse(savedUser);
	}

	public List<UserListResponse> getAllUsers() {
		return userRepository.findByActiveTrue().stream()
				.map(UserListResponse::new)
				.collect(Collectors.toList());
	}

	public UserListResponse getUserById(UUID id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (!user.getActive()) {
			throw new RuntimeException("User is inactive");
		}

		return new UserListResponse(user);
	}

	@Transactional
	public UserListResponse updateUser(UUID id, UpdateUserRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (!user.getActive()) {
			throw new RuntimeException("Cannot update inactive user");
		}

		if (request.getEmail() != null && !request.getEmail().isEmpty()) {
			if (!user.getEmail().equals(request.getEmail()) &&
					userRepository.findByEmail(request.getEmail()).isPresent()) {
				throw new RuntimeException("Email already taken");
			}
			user.setEmail(request.getEmail());
		}

		if (request.getPassword() != null && !request.getPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}

		if (request.getRole() != null) {
			user.setRole(request.getRole());
		}

		User updatedUser = userRepository.save(user);
		return new UserListResponse(updatedUser);
	}

	@Transactional
	public void deleteUser(UUID id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));

		user.setActive(false);
		userRepository.save(user);
	}

	public long getUserCount() {
		return userRepository.countByActiveTrue();
	}

	public List<UserListResponse> getUsersByRole(Role role) {
		return userRepository.findByRoleAndActiveTrue(role).stream()
				.map(UserListResponse::new)
				.collect(Collectors.toList());
	}

	public List<UserListResponse> getUsersByAdmin(UUID adminId) {
		User admin = userRepository.findById(adminId)
				.orElseThrow(() -> new RuntimeException("Admin not found"));

		return userRepository.findByAdminAndActiveTrue(admin).stream()
				.map(UserListResponse::new)
				.collect(Collectors.toList());
	}
}
