package com.streamly.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.streamly.dto.AppUser;

public interface UserRepo extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByEmail(String email); // Optional = "might be empty", forces us to handle not-found

	Optional<AppUser> findByToken(String token); // used to turn a token into a user
}
