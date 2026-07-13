package com.ines.skillmatch_auth_service.repository.jpa;

import com.ines.skillmatch_auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for User entities.
 * Provides CRUD operations and custom query methods for user management.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Finds a user by their email address (unique field).
    Optional<User> findByEmail(String email);

    // Finds a user by OAuth2 provider and provider ID (for Google/GitHub login).
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // Checks if a user with the given email already exists (returns true/false).
    Boolean existsByEmail(String email);

    // Returns the 5 most recently created users (for admin dashboard "latest users").
    List<User> findTop5ByOrderByDateCreationDesc();

    // Counts all users with a specific role (CANDIDAT, ENTREPRISE, ADMIN).
    long countByRole(User.Role role);
}