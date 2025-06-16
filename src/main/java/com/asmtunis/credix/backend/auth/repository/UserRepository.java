package com.asmtunis.credix.backend.auth.repository;

import com.asmtunis.credix.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmail(String email);

	List<User> findByCorporate(User corporate);

	List<User> findByCorporateEmail(String corporateEmail);
}
