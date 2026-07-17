package com.ines.skillmatch_auth_service.security;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
/**
 * Custom OAuth2 authentication success handler.
 *
 * This component is invoked after a successful OAuth2 login (e.g., via Google, GitHub, etc.).
 * It generates a JWT token for the authenticated user and redirects the client
 * (frontend) to a callback URL with the token and user details as query parameters.
 *
 * The frontend can then extract these parameters, store the token, and create a
 * local session.
 *
 * Extends {@link SimpleUrlAuthenticationSuccessHandler} to leverage its
 * built-in redirect capabilities.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    /**
     * Handles the authentication success event after a successful OAuth2 login.
     *
     * The method:
     * 1. Retrieves the authenticated principal as {@link UserDetailsImpl}.
     * 2. Generates a JWT token using the user's details.
     * 3. Constructs a redirect URL to the frontend's callback endpoint,
     *    appending the token, email, role, and user ID as query parameters.
     * 4. Redirects the user to the frontend URL.
     *
     * The frontend is expected to:
     * - Parse the query parameters.
     * - Store the JWT token (e.g., in localStorage or a secure HTTP-only cookie).
     * - Redirect the user to the appropriate dashboard or home page.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param authentication the Authentication object containing the user details
     * @throws IOException if an I/O error occurs during redirection
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        // On redirige vers le port du FRONTEND pour que le frontend crée la session
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8086/oauth2/callback")
                .queryParam("token", token)
                .queryParam("email", userDetails.getEmail())
                .queryParam("role", userDetails.getRole().name())
                .queryParam("userId", userDetails.getId())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
