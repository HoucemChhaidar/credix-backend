package com.asmtunis.credix.backend.features.user.repository;

import com.asmtunis.credix.backend.features.user.entity.Role;
import com.asmtunis.credix.backend.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmail(String email);

	List<User> findByActiveTrue();

	List<User> findByRoleAndActiveTrue(Role role);

	List<User> findByAdminAndActiveTrue(User admin);

	long countByActiveTrue();
}
