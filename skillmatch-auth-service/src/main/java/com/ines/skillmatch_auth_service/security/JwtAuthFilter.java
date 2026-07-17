package com.ines.skillmatch_auth_service.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/**
 * JWT Authentication Filter that intercepts each HTTP request once per request
 * and validates JWT tokens present in the Authorization header.
 *
 * This filter:
 * 1. Extracts the JWT token from the "Authorization: Bearer ..." header.
 * 2. Validates the token (signature and expiration).
 * 3. If valid, loads the user details from the database using the email from the token.
 * 4. Sets the authentication in the Spring Security context so that the request
 *    is considered authenticated.
 *
 * It extends {@link OncePerRequestFilter} to guarantee that the filter is executed
 * only once per request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    /**
     * Performs the JWT authentication validation for each incoming request.
     *
     * The method:
     * - Extracts the token from the Authorization header.
     * - If a token exists and is valid, it extracts the user's email.
     * - Loads the corresponding UserDetails from the database.
     * - Creates an authenticated UsernamePasswordAuthenticationToken and stores
     *   it in the SecurityContextHolder.
     * - On any exception (invalid token, user not found, etc.), it clears the
     *   security context to prevent unauthorized access.
     * - Finally, it continues the filter chain.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain to pass the request along
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            // Si le token existe et est valide
            if (token != null && jwtService.isTokenValid(token)) {
                String email = jwtService.extractEmail(token);

                // On charge l'utilisateur depuis la base de données
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // On crée l'objet d'authentification pour Spring Security
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ON "IDENTIFIE" l'utilisateur dans le système
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // En cas d'erreur de token, on efface le contexte par sécurité
            SecurityContextHolder.clearContext();
            // Optionnel : tu peux logger l'erreur ici pour ton debug
            logger.error("Impossible de définir l'authentification : {}");
        }

        // On laisse la requête continuer son chemin
        filterChain.doFilter(request, response);
    }
    /**
     * Extracts the JWT token from the HTTP Authorization header.
     *
     * The header must follow the format: "Bearer <token>".
     * If the header is missing or does not start with "Bearer ", returns null.
     *
     * @param request the HTTP request
     * @return the extracted JWT token, or null if not present or invalid format
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
