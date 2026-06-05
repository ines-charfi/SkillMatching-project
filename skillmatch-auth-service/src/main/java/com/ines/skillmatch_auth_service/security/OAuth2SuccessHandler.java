package com.ines.skillmatch_auth_service.security;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

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
