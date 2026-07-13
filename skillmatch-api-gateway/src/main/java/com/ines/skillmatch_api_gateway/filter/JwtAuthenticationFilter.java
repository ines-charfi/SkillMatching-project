package com.ines.skillmatch_api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Gateway filter that validates JWT tokens on incoming requests.
 * It intercepts all requests (except public endpoints like login/register),
 * verifies the token, and forwards user details to downstream services.
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // 1. Allow OPTIONS preflight requests (CORS handshake) to pass without authentication
            if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
                return chain.filter(exchange);
            }

            String path = exchange.getRequest().getURI().getPath();

            // 2. Bypass authentication for public endpoints (login, register, OAuth2)
            if (path.contains("/api/auth/login") || path.contains("/api/auth/register") || path.contains("/oauth2/")) {
                return chain.filter(exchange);
            }

            // 3. Ensure the Authorization header is present
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = exchange.getRequest().getHeaders()
                    .get(HttpHeaders.AUTHORIZATION).get(0);

            // 4. Verify that the header starts with "Bearer "
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // 5. Extract the token (remove "Bearer " prefix)
            String token = authHeader.substring(7);

            try {
                // 6. Validate the token by parsing its claims using the secret key
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // 7. Inject user information into the request headers so that downstream
                //    microservices can access the authenticated user's ID, role, and email
                exchange.getRequest().mutate()
                        .header("X-User-Id", String.valueOf(claims.get("userId")))
                        .header("X-User-Role", claims.get("role", String.class))
                        .header("X-User-Email", claims.getSubject())
                        .build();

                // 8. Continue the filter chain (forward request to the target service)
                return chain.filter(exchange);

            } catch (Exception e) {
                // 9. If token validation fails, return 401 Unauthorized
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    /**
     * Decodes the Base64‑encoded secret key and builds a {@link SecretKey} for JWT validation.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static class Config {
        // Empty config class – can be extended if needed
    }
}