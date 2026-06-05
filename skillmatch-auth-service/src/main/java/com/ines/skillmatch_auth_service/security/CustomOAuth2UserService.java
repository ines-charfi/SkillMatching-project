package com.ines.skillmatch_auth_service.security;

import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import com.ines.skillmatch_auth_service.service.client.CandidatClient; // AJOUT
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final CandidatClient candidatClient; // AJOUT : Pour initialiser le profil

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

        // On cherche l'utilisateur, s'il n'existe pas, on le crée ET on initialise son profil
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(oAuth2User, email, registrationId, providerId));

        if (!registrationId.equals(user.getProvider())) {
            user.setProvider(registrationId);
            user.setProviderId(providerId);
            userRepository.save(user);
        }

        return UserDetailsImpl.build(user, oAuth2User.getAttributes());
    }

    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        return oAuth2User.getAttribute("email"); // Simplifié pour l'exemple
    }

    private User createNewUser(OAuth2User oAuth2User, String email, String provider, String providerId) {
        // 1. Créer l'utilisateur Auth
        User user = User.builder()
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.CANDIDAT) // Par défaut, un utilisateur social est un candidat
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // 2. RÉCUPÉRER LE NOM ET PRÉNOM DEPUIS GOOGLE/GITHUB
        String name = oAuth2User.getAttribute("name");
        String firstName = "";
        String lastName = name != null ? name : "Utilisateur";

        if (name != null && name.contains(" ")) {
            firstName = name.split(" ")[0];
            lastName = name.split(" ")[1];
        }

        // 3. INITIALISER LE PROFIL DANS LE MICROSERVICE CANDIDAT
        try {
            candidatClient.initCandidat(savedUser.getId(), lastName, firstName);
        } catch (Exception e) {
            System.err.println("Erreur init profil OAuth2: " + e.getMessage());
        }

        return savedUser;
    }
}