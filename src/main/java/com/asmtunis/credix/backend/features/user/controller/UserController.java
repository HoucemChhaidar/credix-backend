package com.asmtunis.credix.backend.features.user.controller;

import com.asmtunis.credix.backend.common.dto.ResponseWrapper;
import com.asmtunis.credix.backend.features.user.dto.request.CreateUserRequest;
import com.asmtunis.credix.backend.features.user.dto.request.UpdateUserRequest;
import com.asmtunis.credix.backend.features.user.dto.response.UserListResponse;
import com.asmtunis.credix.backend.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.asmtunis.credix.backend.features.user.entity.Role;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/bootstrap")
	@Operation(summary = "Bootstrap first SUPER_ADMIN", description = "Create the first SUPER_ADMIN user. Only works if no SUPER_ADMIN exists.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "SUPER_ADMIN created successfully"),
			@ApiResponse(responseCode = "400", description = "SUPER_ADMIN already exists or validation error")
	})
	public ResponseEntity<ResponseWrapper<UserListResponse>> bootstrapSuperAdmin(@Valid @RequestBody CreateUserRequest request) {
		UserListResponse user = userService.bootstrapSuperAdmin(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ResponseWrapper<>(201, "SUPER_ADMIN created successfully", user));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Create new user", description = "Create a new user account. ADMIN can create USERs, SUPER_ADMIN can create ADMINs and USERs")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User created successfully"),
			@ApiResponse(responseCode = "400", description = "Email already taken or validation error"),
			@ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
	})
	public ResponseEntity<ResponseWrapper<UserListResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
		System.out.println("[v0] ===== CreateUser Method Called =====");
		System.out.println("[v0] Request email: " + request.getEmail());
		System.out.println("[v0] Request role: " + request.getRole());

		UserListResponse user = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ResponseWrapper<>(201, "User created successfully", user));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Get all users", description = "Retrieve a list of all users in the system")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
	})
	public ResponseEntity<ResponseWrapper<List<UserListResponse>>> getAllUsers() {
		List<UserListResponse> users = userService.getAllUsers();
		return ResponseEntity.ok(new ResponseWrapper<>(200, "Users retrieved successfully", users));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
	})
	public ResponseEntity<ResponseWrapper<UserListResponse>> getUserById(@PathVariable UUID id) {
		UserListResponse user = userService.getUserById(id);
		return ResponseEntity.ok(new ResponseWrapper<>(200, "User retrieved successfully", user));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Update user", description = "Update user information (email, password, or role)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User updated successfully"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "400", description = "Invalid input or email already taken"),
			@ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
	})
	public ResponseEntity<ResponseWrapper<UserListResponse>> updateUser(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateUserRequest request) {
		UserListResponse updatedUser = userService.updateUser(id, request);
		return ResponseEntity.ok(new ResponseWrapper<>(200, "User updated successfully", updatedUser));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Delete user (soft delete)", description = "Soft delete a user by setting active=false. User data is preserved.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User deleted successfully"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
	})
	public ResponseEntity<ResponseWrapper<Void>> deleteUser(@PathVariable UUID id) {
		userService.deleteUser(id);
		return ResponseEntity.ok(new ResponseWrapper<>(200, "User deleted successfully", null));
	}

	@GetMapping("/count")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Get user count", description = "Get the total number of users in the system")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User count retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
	})
	public ResponseEntity<ResponseWrapper<Long>> getUserCount() {
		long count = userService.getUserCount();
		return ResponseEntity.ok(new ResponseWrapper<>(200, "User count retrieved successfully", count));
	}

	@GetMapping("/role/{role}")
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@Operation(summary = "Get users by role", description = "Retrieve all active users with a specific role (SUPER_ADMIN only)")
	public ResponseEntity<ResponseWrapper<List<UserListResponse>>> getUsersByRole(@PathVariable Role role) {
		List<UserListResponse> users = userService.getUsersByRole(role);
		return ResponseEntity.ok(new ResponseWrapper<>(200, "Users retrieved successfully", users));
	}

	@GetMapping("/admin/{adminId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Get users by admin", description = "Retrieve all active users under a specific admin")
	public ResponseEntity<ResponseWrapper<List<UserListResponse>>> getUsersByAdmin(@PathVariable UUID adminId) {
		List<UserListResponse> users = userService.getUsersByAdmin(adminId);
		return ResponseEntity.ok(new ResponseWrapper<>(200, "Users retrieved successfully", users));
	}

	@GetMapping("/me")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Get current user", description = "Retrieve the currently authenticated user's information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "404", description = "User not found")
	})
	public ResponseEntity<ResponseWrapper<UserListResponse>> getCurrentUser(Authentication authentication) {
		String userEmail = authentication.getName();
		UserListResponse user = userService.getCurrentUser(userEmail);
		return ResponseEntity.ok(new ResponseWrapper<>(200, "User retrieved successfully", user));
	}
}
