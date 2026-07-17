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
/**
 * Custom OAuth2 user service that handles OAuth2 login flows (Google, GitHub, etc.).
 *
 * This service is invoked by Spring Security during the OAuth2 authentication process.
 * It:
 * 1. Fetches the user's information from the OAuth2 provider.
 * 2. Checks if the user already exists in the local database.
 * 3. If not, creates a new user account and initializes a candidate profile
 *    in the Candidate microservice.
 * 4. Updates the provider information if the user already exists but uses a
 *    different OAuth2 provider.
 * 5. Returns a {@link UserDetailsImpl} object that integrates with Spring Security.
 *
 * This is the bridge between the external OAuth2 provider and the internal
 * authentication system.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final CandidatClient candidatClient; // AJOUT : Pour initialiser le profil
    /**
     * Loads the OAuth2 user from the provider and processes it for local storage.
     *
     * The method:
     * 1. Delegates to {@link DefaultOAuth2UserService} to fetch the user info
     *    from the provider.
     * 2. Extracts the email and provider ID from the OAuth2 response.
     * 3. Looks up the user in the local database.
     * 4. If the user doesn't exist, creates a new user and triggers the
     *    creation of a candidate profile.
     * 5. If the user exists but the provider has changed, updates the provider info.
     * 6. Returns a {@link UserDetailsImpl} containing the user details and
     *    the OAuth2 attributes.
     *
     * The transaction ensures that user creation and profile initialization
     * happen atomically.
     *
     * @param userRequest the OAuth2 user request containing provider details
     * @return an OAuth2User instance with the authenticated user's data
     * @throws OAuth2AuthenticationException if the email cannot be extracted
     */
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
    /**
     * Extracts the email from the OAuth2 user's attributes.
     *
     * This is a simplified implementation. In a real-world scenario, you may
     * need to handle different attribute names for different providers
     * (e.g., "email" vs "mail").
     *
     * @param oAuth2User the OAuth2 user object from the provider
     * @param registrationId the provider identifier (e.g., "google", "github")
     * @return the extracted email, or null if not found
     */
    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        return oAuth2User.getAttribute("email"); // Simplifié pour l'exemple
    }
    /**
     * Creates a new user in the local database from OAuth2 data and
     * initializes their candidate profile in the Candidate microservice.
     *
     * Steps:
     * 1. Builds a new User entity with the email, provider, and providerId.
     * 2. Sets the default role to CANDIDAT (since OAuth2 users are typically candidates).
     * 3. Saves the user to the database.
     * 4. Extracts the first and last name from the "name" attribute.
     * 5. Calls the Candidate microservice via Feign to initialize the
     *    candidate profile.
     * 6. Logs errors but continues (profile initialization failure does not
     *    block the authentication).
     *
     * @param oAuth2User the OAuth2 user object from the provider
     * @param email the user's email
     * @param provider the provider name (e.g., "google", "github")
     * @param providerId the unique ID from the provider
     * @return the newly created User entity
     */
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