package com.ines.skillmatch_auth_service.repository.jpa;
import com.ines.skillmatch_auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    Boolean existsByEmail(String email);
    List<User> findTop5ByOrderByDateCreationDesc();
    long countByRole(User.Role role);
}
