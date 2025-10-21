package com.asmtunis.credix.backend.features.user.service;

import com.asmtunis.credix.backend.features.user.dto.request.CreateUserRequest;
import com.asmtunis.credix.backend.features.user.dto.request.UpdateUserRequest;
import com.asmtunis.credix.backend.features.user.dto.response.UserListResponse;
import com.asmtunis.credix.backend.features.user.entity.Role;
import com.asmtunis.credix.backend.features.user.entity.User;
import com.asmtunis.credix.backend.features.user.repository.UserRepository;
import com.asmtunis.credix.backend.features.wallet.service.WalletService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final WalletService walletService;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, WalletService walletService) {
		this.userRepository = userRepository;
		this.walletService = walletService;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	@Transactional
	public UserListResponse bootstrapSuperAdmin(CreateUserRequest request) {
		// Check if any SUPER_ADMIN already exists
		if (userRepository.findByRoleAndActiveTrue(Role.SUPER_ADMIN).stream().findAny().isPresent()) {
			throw new RuntimeException("SUPER_ADMIN already exists. Use regular user creation endpoint.");
		}

		// Force role to SUPER_ADMIN
		if (request.getRole() != Role.SUPER_ADMIN) {
			throw new RuntimeException("Bootstrap endpoint only creates SUPER_ADMIN users");
		}

		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("Email already taken");
		}

		User newUser = new User();
		newUser.setEmail(request.getEmail());
		newUser.setPassword(passwordEncoder.encode(request.getPassword()));
		newUser.setRole(Role.SUPER_ADMIN);
		newUser.setActive(true);
		newUser.setFirstName(request.getFirstName());
		newUser.setLastName(request.getLastName());
		newUser.setPhoneNumber(request.getPhoneNumber());
		newUser.setCompanyName(request.getCompanyName());
		newUser.setProfileImageUrl(request.getProfileImageUrl());

		User savedUser = userRepository.save(newUser);
		return new UserListResponse(savedUser);
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
		newUser.setFirstName(request.getFirstName());
		newUser.setLastName(request.getLastName());
		newUser.setPhoneNumber(request.getPhoneNumber());
		newUser.setCompanyName(request.getCompanyName());
		newUser.setProfileImageUrl(request.getProfileImageUrl());

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
			walletService.createWalletForUser(savedUser);
		}

		return new UserListResponse(savedUser);
	}

	public List<UserListResponse> getAllUsers() {
		// Get authenticated user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String authenticatedEmail = authentication.getName();

		User authenticatedUser = userRepository.findByEmail(authenticatedEmail)
				.orElseThrow(() -> new RuntimeException("Authenticated user not found"));

		// Filter based on role
		if (authenticatedUser.getRole() == Role.SUPER_ADMIN) {
			// SUPER_ADMIN sees only their ADMINs
			return userRepository.findByAdminAndActiveTrue(authenticatedUser).stream()
					.filter(user -> user.getRole() == Role.ADMIN)
					.map(UserListResponse::new)
					.collect(Collectors.toList());
		} else if (authenticatedUser.getRole() == Role.ADMIN) {
			// ADMIN sees only their USERs
			return userRepository.findByAdminAndActiveTrue(authenticatedUser).stream()
					.filter(user -> user.getRole() == Role.USER)
					.map(UserListResponse::new)
					.collect(Collectors.toList());
		}

		// USER role shouldn't reach here due to @PreAuthorize, but return empty list as fallback
		return List.of();
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

		if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
			user.setFirstName(request.getFirstName());
		}

		if (request.getLastName() != null && !request.getLastName().isEmpty()) {
			user.setLastName(request.getLastName());
		}

		if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
			user.setPhoneNumber(request.getPhoneNumber());
		}

		if (request.getCompanyName() != null) {
			user.setCompanyName(request.getCompanyName());
		}

		if (request.getProfileImageUrl() != null) {
			user.setProfileImageUrl(request.getProfileImageUrl());
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
