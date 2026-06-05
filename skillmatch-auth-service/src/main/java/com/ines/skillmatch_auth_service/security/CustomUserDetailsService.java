package com.ines.skillmatch_auth_service.security;

import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // Optimisation : indique à Hibernate que c'est une lecture seule
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche l'utilisateur par email
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