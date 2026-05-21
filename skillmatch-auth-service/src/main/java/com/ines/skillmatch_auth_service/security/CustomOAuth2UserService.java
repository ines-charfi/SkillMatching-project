package com.ines.skillmatch_auth_service.security;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = extractEmail(oAuth2User, registrationId);
        String providerId = oAuth2User.getName();

        if (email == null) {
            throw new OAuth2AuthenticationException("Email non trouvé depuis " + registrationId);
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, registrationId, providerId));

        // Mettre à jour le provider si l'utilisateur utilise une nouvelle méthode
        if (!registrationId.equals(user.getProvider())) {
            user.setProvider(registrationId);
            user.setProviderId(providerId);
            userRepository.save(user);
        }

        return UserDetailsImpl.build(user, oAuth2User.getAttributes());
    }

    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        if ("github".equals(registrationId)) {
            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                // GitHub peut retourner l'email dans un format différent
                @SuppressWarnings("unchecked")
                Map<String, Object> emails = (Map<String, Object>) oAuth2User.getAttributes().get("email");
                email = emails != null ? (String) emails.get("email") : null;
            }
            if (email == null) {
                // Fallback : utiliser le login GitHub comme email
                String login = oAuth2User.getAttribute("login");
                email = login + "@github.com";
            }
            return email;
        }
        return oAuth2User.getAttribute("email");
    }

    private User createNewUser(String email, String provider, String providerId) {
        User user = User.builder()
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.CANDIDAT) // Rôle par défaut
                .enabled(true)
                .build();
        return userRepository.save(user);
    }
}
