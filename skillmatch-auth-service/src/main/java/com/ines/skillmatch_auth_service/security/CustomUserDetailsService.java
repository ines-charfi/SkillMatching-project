package com.ines.skillmatch_auth_service.security;

import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 *
 * This service is responsible for loading user-specific data from the database
 * during authentication. It bridges the {@link User} entity (JPA) and the
 * Spring Security {@link UserDetails} interface via {@link UserDetailsImpl}.
 *
 * It is primarily used by:
 * - {@link JwtAuthFilter} to load user details from a JWT token.
 * - Spring Security's authentication provider during login.
 *
 * The service also enforces account status checks (e.g., whether the account
 * is enabled by an administrator).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    /**
     * Loads a user by their email address (which is used as the username).
     *
     * This method is required by the {@link UserDetailsService} interface
     * and is called by Spring Security during authentication and token validation.
     *
     * The method:
     * 1. Searches for a user with the given email.
     * 2. Throws {@link UsernameNotFoundException} if no user is found.
     * 3. Throws {@link DisabledException} if the account has been disabled by an admin.
     * 4. Converts the {@link User} entity to {@link UserDetailsImpl} for
     *    integration with Spring Security.
     *
     * The transaction is marked as read-only for performance optimization,
     * since this operation only reads data and does not modify anything.
     *
     * @param email the user's email address (used as the username)
     * @return a UserDetails object containing the user's authentication data
     * @throws UsernameNotFoundException if no user is found with the given email
     * @throws DisabledException if the user's account is disabled
     */
    @Override
    @Transactional(readOnly = true) // Optimisation : indique à Hibernate que c'est une lecture seule
    /**
     * Loads a user by their internal ID.
     *
     * This is a convenience method that is not required by the UserDetailsService
     * interface, but it is useful when you need to retrieve user details
     * by ID (e.g., from other services or for administrative purposes).
     *
     * @param id the user's internal ID
     * @return the UserDetailsImpl containing the user's data
     * @throws UsernameNotFoundException if no user is found with the given ID
     */
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur trouvé avec l'email : " + email));

        // Vérification si le compte est activé (très important pour l'admin)
        if (!user.getEnabled()) {
            throw new DisabledException("Ce compte a été désactivé par l'administrateur.");
        }

        return UserDetailsImpl.build(user);
    }

    @Transactional(readOnly = true)
    public UserDetailsImpl loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé avec l'ID : " + id));

        return UserDetailsImpl.build(user);
    }
}